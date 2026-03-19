package com.beaker.Lab03.backend.controller;

import com.beaker.Lab03.backend.dto.GameBoardResponse;
import com.beaker.Lab03.backend.dto.MatchResult;
import com.beaker.Lab03.backend.dto.ShuffleMapRequest;
import com.beaker.Lab03.backend.service.LinkService;
import com.beaker.Lab03.backend.dto.MatchCheckRequest;
import com.beaker.Lab03.backend.dto.MatchCheckResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 欢乐连连看匹配接口。
 */
@RestController
@RequestMapping("/api/game")
public class LinkController {

    private final LinkService linkService;

    public LinkController(LinkService linkService) {
        this.linkService = linkService;
    }

    @GetMapping("/init")
    public ResponseEntity<GameBoardResponse> initGame() {
        return ResponseEntity.ok(GameBoardResponse.of(true, "棋盘初始化成功", linkService.initMap()));
    }

    @PostMapping("/shuffle")
    public ResponseEntity<GameBoardResponse> shuffleGame(@RequestBody ShuffleMapRequest request) {
        if (request == null || request.getMap() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(GameBoardResponse.of(false, "请求参数不完整", null));
        }

        int[][] shuffledMap = linkService.shuffleMap(request.getMap());
        if (shuffledMap == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(GameBoardResponse.of(false, "地图格式不合法", null));
        }

        return ResponseEntity.ok(GameBoardResponse.of(true, "棋盘打乱成功", shuffledMap));
    }

    @PostMapping("/match-check")
    public ResponseEntity<MatchCheckResponse> checkMatch(@RequestBody MatchCheckRequest request) {
        if (request == null || request.getMap() == null || request.getV1() == null || request.getV2() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(MatchCheckResponse.of(false, "请求参数不完整", null, null));
        }

        MatchResult result = linkService.checkMatch(request.getMap(), request.getV1(), request.getV2());
        String message = result.isConnected() ? "匹配成功" : "当前两个方块无法连通";

        return ResponseEntity.ok(
                MatchCheckResponse.of(result.isConnected(), message, request.getMap(), result.getPath())
        );
    }
}
