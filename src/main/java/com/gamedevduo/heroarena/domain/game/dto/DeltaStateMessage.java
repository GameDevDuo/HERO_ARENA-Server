package com.gamedevduo.heroarena.domain.game.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeltaStateMessage { //서버 → 클라이언트

    private String type; // "DELTA_STATE_SYNC"
    private long timestamp;
    private List<PlayerState> players;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlayerState {
        private String userId;
        private String action;
        private String character;
        private PlayerUpdateMessage.Position position;
        private PlayerUpdateMessage.Rotation rotation;
        private int health;
    }
}