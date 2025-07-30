package com.gamedevduo.heroarena.domain.game.dto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GameRoomCreateRequest {

    @NotBlank(message = "방 제목은 필수입니다.")
    private String roomTitle;

    @NotBlank(message = "게임 모드는 필수입니다.")
    private String gameMode;

    @Min(value = 2, message = "최소 2명 이상이어야 합니다.")
    private int maxPlayers;
}