package com.gamedevduo.heroarena.domain.game.service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamedevduo.heroarena.domain.game.dto.DeltaStateMessage;
import com.gamedevduo.heroarena.domain.game.dto.GameRoomInfo;
import com.gamedevduo.heroarena.domain.game.dto.JoinResult;
import com.gamedevduo.heroarena.domain.game.dto.PlayerUpdateMessage;
import com.gamedevduo.heroarena.global.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameRoomService {

    private final RedisUtil redisUtil;
    private final ObjectMapper objectMapper;
    private static final String ROOM_LIST_KEY = "rooms";                // 모든 방 ID 리스트
    private static final String ROOM_INFO_KEY = "room:%s:info";         // 특정 방 정보
    private static final String ROOM_PLAYERS_KEY = "room:%s:players";   // 특정 방 플레이어 상태
    private final Map<String, Map<String, Integer>> lastStateHashCache = new ConcurrentHashMap<>();

    /** 방 생성 */
    public GameRoomInfo createRoom(String roomTitle, String gameMode, int maxPlayers) {
        String roomId = UUID.randomUUID().toString(); // 고유한 방 ID 생성

        GameRoomInfo info = GameRoomInfo.builder()
                .roomId(roomId)
                .roomTitle(roomTitle)
                .gameMode(gameMode)
                .currentPlayers(0)
                .maxPlayers(maxPlayers)
                .createdAt(System.currentTimeMillis())
                .build();

        try {
            redisUtil.addToList(ROOM_LIST_KEY, roomId);  // 방 ID를 전체 리스트에 추가
            redisUtil.setValue(String.format(ROOM_INFO_KEY, roomId), objectMapper.writeValueAsString(info));
            redisUtil.setValue(String.format(ROOM_PLAYERS_KEY, roomId), "{}"); // 플레이어 상태 초기화
        } catch (JsonProcessingException e) {
            throw new RuntimeException("방 생성 실패", e);
        }

        log.info("Room created: {} ({})", roomId, roomTitle);
        return info;
    }

    /** 방 삭제 */
    public void removeRoom(String roomId) {
        redisUtil.removeFromList(ROOM_LIST_KEY, roomId);
        redisUtil.delete(String.format(ROOM_INFO_KEY, roomId));
        redisUtil.delete(String.format(ROOM_PLAYERS_KEY, roomId));
        lastStateHashCache.remove(roomId); // 캐시 제거
        log.info("Room removed: {}", roomId);
    }

    /** 방 정보 가져오기 */
    public Optional<GameRoomInfo> getRoomInfo(String roomId) {
        return redisUtil.getValue(String.format(ROOM_INFO_KEY, roomId))
                .map(data -> {
                    try {
                        return objectMapper.readValue(data, GameRoomInfo.class);
                    } catch (JsonProcessingException e) {
                        log.error("Failed to parse room info: {}", data);
                        return null;
                    }
                });
    }

    /** 방 리스트 반환 */
    public List<GameRoomInfo> getRoomList() {
        List<String> roomIds = redisUtil.getList(ROOM_LIST_KEY);
        List<GameRoomInfo> rooms = new ArrayList<>();

        for (String roomId : roomIds) {
            getRoomInfo(roomId).ifPresent(rooms::add);
        }

        return rooms;
    }

    /** 방 입장 */
    public JoinResult joinRoom(String roomId) {
        Optional<GameRoomInfo> infoOpt = getRoomInfo(roomId);
        if (infoOpt.isEmpty()) return new JoinResult(false, false);

        GameRoomInfo info = infoOpt.get();
        if (info.getCurrentPlayers() >= info.getMaxPlayers()) {
            return new JoinResult(false, true);
        }

        info.setCurrentPlayers(info.getCurrentPlayers() + 1);
        saveRoomInfo(info);

        boolean isFullNow = info.getCurrentPlayers() == info.getMaxPlayers();
        return new JoinResult(true, isFullNow);
    }

    /** 방 퇴장 */
    public void leaveRoom(String roomId) {
        Optional<GameRoomInfo> infoOpt = getRoomInfo(roomId);
        if (infoOpt.isEmpty()) return;

        GameRoomInfo info = infoOpt.get();
        int newCount = Math.max(0, info.getCurrentPlayers() - 1);
        info.setCurrentPlayers(newCount);

        if (newCount == 0) {
            removeRoom(roomId); // 마지막 사람이 나가면 방 삭제
        } else {
            saveRoomInfo(info);
        }
    }

    /** 방 정보 저장 */
    private void saveRoomInfo(GameRoomInfo info) {
        try {
            redisUtil.setValue(String.format(ROOM_INFO_KEY, info.getRoomId()),
                    objectMapper.writeValueAsString(info));
        } catch (JsonProcessingException e) {
            log.error("Failed to save room info", e);
        }
    }

    // ========== 플레이어 실시간 상태 동기화 ==========

    /** 플레이어 상태 저장/갱신 */
    public void updatePlayerState(PlayerUpdateMessage update) {
        Map<String, Object> players = getRoomPlayers(update.getRoomId());

        Map<String, Object> state = Map.of(
                "position", update.getPosition(),
                "rotation", update.getRotation(),
                "action", update.getAction(),
                "character", update.getCharacter(),
                "health", update.getHealth(),
                "lastUpdated", System.currentTimeMillis()
        );

        players.put(update.getUserId(), state);
        saveRoomPlayers(update.getRoomId(), players);
    }

    /**
     * DeltaStateMessage 생성 (변경된 플레이어만 포함)
     */
    public DeltaStateMessage getDeltaStates(String roomId) {
        Map<String, Object> players = getRoomPlayers(roomId);

        // 이전 상태 해시 맵 가져오기
        Map<String, Integer> lastHashes = lastStateHashCache.computeIfAbsent(roomId, k -> new ConcurrentHashMap<>());
        Map<String, Integer> newHashes = new HashMap<>();

        List<DeltaStateMessage.PlayerState> changedPlayers = players.entrySet().stream()
                .filter(entry -> {
                    Map<String, Object> state = (Map<String, Object>) entry.getValue();
                    int currentHash = state.hashCode();
                    newHashes.put(entry.getKey(), currentHash);

                    Integer oldHash = lastHashes.get(entry.getKey());
                    return oldHash == null || !oldHash.equals(currentHash);
                })
                .map(entry -> {
                    Map<String, Object> state = (Map<String, Object>) entry.getValue();
                    PlayerUpdateMessage.Position position =
                            objectMapper.convertValue(state.get("position"), PlayerUpdateMessage.Position.class);
                    PlayerUpdateMessage.Rotation rotation =
                            objectMapper.convertValue(state.get("rotation"), PlayerUpdateMessage.Rotation.class);

                    return DeltaStateMessage.PlayerState.builder()
                            .userId(entry.getKey())
                            .position(position)
                            .rotation(rotation)
                            .action((String) state.get("action"))
                            .character((String) state.get("character"))
                            .health((Integer) state.get("health"))
                            .build();
                })
                .collect(Collectors.toList());

        // 캐시 갱신
        lastStateHashCache.put(roomId, newHashes);

        return DeltaStateMessage.builder()
                .type("DELTA_STATE_SYNC")
                .timestamp(System.currentTimeMillis())
                .players(changedPlayers)
                .build();
    }

    /** 플레이어 상태 제거 */
    public void removePlayerState(String roomId, String userId) {
        Map<String, Object> players = getRoomPlayers(roomId);
        if (players.remove(userId) != null) {
            saveRoomPlayers(roomId, players);
            log.info("플레이어 {}의 상태가 방 {}에서 제거됨", userId, roomId);
        }

        Map<String, Integer> lastHashes = lastStateHashCache.get(roomId);
        if (lastHashes != null) {
            lastHashes.remove(userId);
        }
    }

    // 내부 유틸
    private Map<String, Object> getRoomPlayers(String roomId) {
        Optional<String> data = redisUtil.getValue(String.format(ROOM_PLAYERS_KEY, roomId));
        if (data.isEmpty()) return new HashMap<>();
        try {
            return objectMapper.readValue(data.get(), Map.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse room data: {}", data.get());
            return new HashMap<>();
        }
    }

    private void saveRoomPlayers(String roomId, Map<String, Object> players) {
        try {
            redisUtil.setValue(String.format(ROOM_PLAYERS_KEY, roomId),
                    objectMapper.writeValueAsString(players));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize room data", e);
        }
    }
}