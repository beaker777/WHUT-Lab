package com.beaker.Lab03.backend.controller;

import com.beaker.Lab03.backend.model.dto.GameBoardResponse;
import com.beaker.Lab03.backend.model.dto.MatchCheckRequest;
import com.beaker.Lab03.backend.model.dto.MatchCheckResponse;
import com.beaker.Lab03.backend.model.dto.MatchResult;
import com.beaker.Lab03.backend.model.dto.ShuffleMapRequest;
import com.beaker.Lab03.backend.service.LinkBoardInitializationService;
import com.beaker.Lab03.backend.service.LinkMatchCheckService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 欢乐连连看 HTTP 接口。
 * 对前端暴露的职责包括：初始化棋盘、洗牌、以及两点消除校验。
 */
@RestController
@RequestMapping("/api/game")
public class LinkController {

    private final LinkBoardInitializationService boardInitializationService;
    private final LinkMatchCheckService matchCheckService;

    public LinkController(
            LinkBoardInitializationService boardInitializationService,
            LinkMatchCheckService matchCheckService
    ) {
        this.boardInitializationService = boardInitializationService;
        this.matchCheckService = matchCheckService;
    }

    /**
     * 创建一局新的游戏棋盘。
     */
    @GetMapping("/init")
    public ResponseEntity<GameBoardResponse> initGame() {
        return ResponseEntity.ok(
                GameBoardResponse.of(true, "棋盘初始化成功", boardInitializationService.initMap())
        );
    }

    /**
     * 仅重排当前仍然存在的图块，不回填已经被消除的空白格。
     */
    @PostMapping("/shuffle")
    public ResponseEntity<GameBoardResponse> shuffleGame(@RequestBody ShuffleMapRequest request) {
        if (request == null || request.getMap() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(GameBoardResponse.of(false, "请求参数不完整", null));
        }

        int[][] shuffledMap = boardInitializationService.shuffleMap(request.getMap());
        if (shuffledMap == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(GameBoardResponse.of(false, "地图格式不合法", null));
        }

        return ResponseEntity.ok(GameBoardResponse.of(true, "棋盘打乱成功", shuffledMap));
    }

    /**
     * 校验前端当前选中的两个格子是否符合连连看消除规则。
     */
    @PostMapping("/match-check")
    public ResponseEntity<MatchCheckResponse> checkMatch(@RequestBody MatchCheckRequest request) {
        if (request == null || request.getMap() == null || request.getV1() == null || request.getV2() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(MatchCheckResponse.of(false, "请求参数不完整", null, null));
        }

        MatchResult result = matchCheckService.checkMatch(request.getMap(), request.getV1(), request.getV2());
        String message = result.isConnected() ? "匹配成功" : "当前两个方块无法连通";

        return ResponseEntity.ok(
                MatchCheckResponse.of(result.isConnected(), message, request.getMap(), result.getPath())
        );
    }
}
