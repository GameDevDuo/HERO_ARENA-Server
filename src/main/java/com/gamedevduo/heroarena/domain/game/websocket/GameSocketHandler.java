package com.gamedevduo.heroarena.domain.game.websocket;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamedevduo.heroarena.domain.game.dto.DeltaStateMessage;
import com.gamedevduo.heroarena.domain.game.dto.JoinResult;
import com.gamedevduo.heroarena.domain.game.dto.PlayerUpdateMessage;
import com.gamedevduo.heroarena.domain.game.service.GameRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameSocketHandler extends TextWebSocketHandler {

    private final GameRoomService gameRoomService;
    private final ObjectMapper objectMapper;

    /** 방별 WebSocket 세션 관리 (roomId -> 세션 목록) */
    private final Map<String, Set<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();

    /** 각 세션이 어떤 방에 속하는지 역매핑 (sessionId -> roomId) */
    private final Map<String, String> sessionRoomMap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String userId = (String) session.getAttributes().get("userId");
        log.info("WebSocket 연결 성공 - userId: {}, sessionId: {}", userId, session.getId());
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            String payload = message.getPayload();
            JsonNode jsonNode = objectMapper.readTree(payload);

            String type = jsonNode.has("type") ? jsonNode.get("type").asText() : "";

            switch (type) {
                case "JOIN_ROOM":
                    handleJoinRoom(session, jsonNode);
                    break;
                case "PLAYER_UPDATE":
                    handlePlayerUpdate(session, jsonNode);
                    break;
                case "LEAVE_ROOM":
                    handleLeaveRoom(session);
                    break;
                case "GAME_END":
                    handleGameEnd(session, jsonNode);
                    break;
                default:
                    log.warn("알 수 없는 메시지 타입: {}", type);
                    sendErrorMessage(session, "UNKNOWN_TYPE", "알 수 없는 메시지 타입입니다: " + type);
            }
        } catch (Exception e) {
            log.error("WebSocket 메시지 처리 실패: {}", e.getMessage(), e);
            sendErrorMessage(session, "MESSAGE_ERROR", "메시지 처리 중 오류가 발생했습니다.");
        }
    }

    private void handleJoinRoom(WebSocketSession session, JsonNode jsonNode) {
        try {
            if (!jsonNode.has("roomId")) {
                log.warn("JOIN_ROOM 요청에 roomId가 없습니다.");
                sendErrorMessage(session, "JOIN_FAILED", "roomId가 없습니다.");
                return;
            }

            String roomId = jsonNode.get("roomId").asText();

            JoinResult joinResult = gameRoomService.joinRoom(roomId);

            if (!joinResult.joined()) {
                log.warn("방 {} 입장 실패 (꽉 찼거나 없음)", roomId);
                sendErrorMessage(session, "JOIN_FAILED", "방이 꽉 찼거나 존재하지 않습니다.");
                return;
            }

            addSessionToRoom(roomId, session);
            sessionRoomMap.put(session.getId(), roomId);

            Map<String, Object> response = Map.of(
                    "type", "JOIN_SUCCESS",
                    "roomId", roomId,
                    "message", "방 입장 성공"
            );
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));

            log.info("세션 {}이 방 {}에 JOIN_ROOM 메시지로 참가", session.getId(), roomId);
            if (joinResult.isRoomFull()) {
                Map<String, Object> fullMsg = Map.of(
                        "type", "ROOM_FULL",
                        "roomId", roomId,
                        "message", "방 정원이 모두 찼습니다."
                );
                broadcastToRoom(roomId, objectMapper.writeValueAsString(fullMsg));
            }
        } catch (Exception e) {
            log.error("JOIN_ROOM 처리 중 오류 발생: {}", e.getMessage());
            sendErrorMessage(session, "JOIN_FAILED", "서버 오류가 발생했습니다.");
        }
    }

    private void handlePlayerUpdate(WebSocketSession session, JsonNode jsonNode) {
        try {
            String userId = (String) session.getAttributes().get("userId");

            if (!jsonNode.has("roomId")) {
                sendErrorMessage(session, "PLAYER_UPDATE_FAILED", "roomId가 누락되었습니다.");
                return;
            }

            PlayerUpdateMessage update = objectMapper.treeToValue(jsonNode, PlayerUpdateMessage.class);
            update.setUserId(userId);

            gameRoomService.updatePlayerState(update);

            DeltaStateMessage deltaStates = gameRoomService.getDeltaStates(update.getRoomId());
            broadcastDeltaToRoom(update.getRoomId(), deltaStates);

        } catch (Exception e) {
            log.error("PLAYER_UPDATE 처리 실패: {}", e.getMessage(), e);
            sendErrorMessage(session, "PLAYER_UPDATE_FAILED", "플레이어 상태 업데이트 중 오류가 발생했습니다.");
        }
    }

    private void handleLeaveRoom(WebSocketSession session) {
        try {
            String sessionId = session.getId();
            String roomId = sessionRoomMap.remove(sessionId);

            if (roomId != null) {
                removeSessionFromRoom(roomId, session);
                gameRoomService.leaveRoom(roomId);

                String userId = (String) session.getAttributes().get("userId");
                gameRoomService.removePlayerState(roomId, userId);

                log.info("세션 {}이 방 {}에서 LEAVE_ROOM 메시지로 퇴장", sessionId, roomId);
            } else {
                sendErrorMessage(session, "LEAVE_FAILED", "방 정보를 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            log.error("LEAVE_ROOM 처리 중 오류: {}", e.getMessage(), e);
            sendErrorMessage(session, "LEAVE_FAILED", "방 퇴장 처리 중 오류가 발생했습니다.");
        }
    }

    private void handleGameEnd(WebSocketSession session, JsonNode jsonNode) {
        try {
            if (!jsonNode.has("roomId")) {
                sendErrorMessage(session, "GAME_END_FAILED", "roomId가 없습니다.");
                return;
            }

            String roomId = jsonNode.get("roomId").asText();
            String broadcastMsg = objectMapper.writeValueAsString(jsonNode);
            broadcastToRoom(roomId, broadcastMsg);

            log.info("방 {}에 GAME_END 브로드캐스트 완료", roomId);

            gameRoomService.removeRoom(roomId);
            roomSessions.remove(roomId);

        } catch (Exception e) {
            log.error("GAME_END 처리 실패: {}", e.getMessage(), e);
            sendErrorMessage(session, "GAME_END_FAILED", "게임 종료 처리 중 오류가 발생했습니다.");
        }
    }

    /** 에러 메시지 전송 유틸 */
    private void sendErrorMessage(WebSocketSession session, String type, String message) {
        try {
            Map<String, Object> error = Map.of(
                    "type", type,
                    "message", message
            );
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(error)));
        } catch (IOException e) {
            log.error("에러 메시지 전송 실패: {}", e.getMessage());
        }
    }

    /** 특정 방에 DELTA_STATE_SYNC 메시지를 브로드캐스트 */
    public void broadcastDeltaToRoom(String roomId, DeltaStateMessage deltaMessage) {
        if (deltaMessage.getPlayers().isEmpty()) {
            return;
        }

        Set<WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions == null || sessions.isEmpty()) return;

        try {
            String json = objectMapper.writeValueAsString(deltaMessage);
            TextMessage msg = new TextMessage(json);

            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(msg);
                    } catch (IOException e) {
                        log.error("세션 {} 전송 실패: {}", session.getId(), e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            log.error("브로드캐스트 실패: {}", e.getMessage());
        }
    }

    public void addSessionToRoom(String roomId, WebSocketSession session) {
        roomSessions.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(session);
        log.info("세션 {}이 방 {}에 참가", session.getId(), roomId);
    }

    public void removeSessionFromRoom(String roomId, WebSocketSession session) {
        Set<WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions != null) {
            sessions.remove(session);
            log.info("세션 {}이 방 {}에서 제거됨", session.getId(), roomId);
        }
    }

    private void broadcastToRoom(String roomId, String json) throws IOException {
        Set<WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions == null || sessions.isEmpty()) return;

        TextMessage msg = new TextMessage(json);
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                session.sendMessage(msg);
            }
        }
    }
}