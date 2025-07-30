package com.gamedevduo.heroarena.domain.game.controller;

import com.gamedevduo.heroarena.domain.game.dto.GameRoomCreateRequest;
import com.gamedevduo.heroarena.domain.game.dto.GameRoomInfo;
import com.gamedevduo.heroarena.domain.game.service.GameRoomService;
import com.gamedevduo.heroarena.global.exception.dto.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/room")
@RequiredArgsConstructor
public class GameRoomController {
    private final GameRoomService gameRoomService;

    // 방 생성
    @PostMapping("/create")
    public ResponseEntity<BaseResponse<GameRoomInfo>> createRoom(
            @Valid @RequestBody GameRoomCreateRequest request) {
        GameRoomInfo roomInfo = gameRoomService.createRoom(
                request.getRoomTitle(),
                request.getGameMode(),
                request.getMaxPlayers()
        );
        return ResponseEntity.ok(BaseResponse.success(roomInfo, "방 생성 성공"));
    }

    // 방 리스트 조회
    @GetMapping
    public ResponseEntity<BaseResponse<List<GameRoomInfo>>> getRoomList() {
        List<GameRoomInfo> rooms = gameRoomService.getRoomList();
        return ResponseEntity.ok(BaseResponse.success(rooms, "방 리스트 조회 성공"));
    }
}
