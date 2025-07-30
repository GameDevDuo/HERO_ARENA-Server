package com.gamedevduo.heroarena.domain.game.dto;

import lombok.*;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameRoomInfo {

    // 방 고유 ID (UUID)
    private String roomId;

    // 방 제목 (유저가 직접 입력)
    private String roomTitle;

    // 게임 모드 (예: coop, vs 등)
    private String gameMode;

    // 현재 방에 접속한 플레이어 수
    private int currentPlayers;

    // 방 최대 인원 수(유저가 입력
    private int maxPlayers;

    private long createdAt;
}