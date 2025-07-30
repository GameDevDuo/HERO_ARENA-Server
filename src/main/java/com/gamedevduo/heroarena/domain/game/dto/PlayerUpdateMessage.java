package com.gamedevduo.heroarena.domain.game.dto;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerUpdateMessage { //클라이언트 → 서버

    private String type;   // "PLAYER_UPDATE"
    private String userId;
    private String roomId;

    private Position position;
    private Rotation rotation;
    private String action; // "공격", "스킬1", "궁극기"
    private String character;
    private int health;
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Position {
        private double x;
        private double y;
        private double z;
    }
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Rotation {
        private double x;
        private double y;
        private double z;
    }
}