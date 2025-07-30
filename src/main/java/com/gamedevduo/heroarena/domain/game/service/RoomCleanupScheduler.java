package com.gamedevduo.heroarena.domain.game.service;

import com.gamedevduo.heroarena.domain.game.dto.GameRoomInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomCleanupScheduler {

    private final GameRoomService gameRoomService;

    @Scheduled(fixedRate = 60000)
    public void removeEmptyRooms() {
        long now = System.currentTimeMillis();
        List<GameRoomInfo> rooms = gameRoomService.getRoomList();

        for (GameRoomInfo room : rooms) {
            if (room.getCurrentPlayers() == 0 && (now - room.getCreatedAt()) > 60_000) {
                gameRoomService.removeRoom(room.getRoomId());
                log.info("방 {}이 1분 이상 비어있어 삭제됨", room.getRoomId());
            }
        }
    }
}
