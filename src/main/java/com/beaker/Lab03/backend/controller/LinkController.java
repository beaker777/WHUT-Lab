package com.beaker.Lab03.backend.controller;

import com.beaker.Lab03.backend.model.dto.GameBoardResponse;
import com.beaker.Lab03.backend.model.dto.GameRuleConfig;
import com.beaker.Lab03.backend.model.dto.HintResponse;
import com.beaker.Lab03.backend.model.dto.InitGameRequest;
import com.beaker.Lab03.backend.model.dto.MatchCheckRequest;
import com.beaker.Lab03.backend.model.dto.MatchCheckResponse;
import com.beaker.Lab03.backend.model.dto.MatchResult;
import com.beaker.Lab03.backend.model.dto.ShuffleMapRequest;
import com.beaker.Lab03.backend.model.pojo.Vertex;
import com.beaker.Lab03.backend.service.LinkBoardInitializationService;
import com.beaker.Lab03.backend.service.LinkDeadlockResolutionService;
import com.beaker.Lab03.backend.service.LinkHintService;
import com.beaker.Lab03.backend.service.LinkMatchCheckService;
import com.beaker.Lab03.backend.util.LinkDifficultyPresets;
import com.beaker.Lab03.backend.util.LinkValidationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 欢乐连连看 HTTP 接口。
 * 对前端暴露的职责包括：初始化棋盘、洗牌、提示以及两点消除校验。
 */
@RestController
@RequestMapping("/api/game")
public class LinkController {

    private final LinkBoardInitializationService boardInitializationService;
    private final LinkDeadlockResolutionService deadlockResolutionService;
    private final LinkHintService hintService;
    private final LinkMatchCheckService matchCheckService;

    public LinkController(
            LinkBoardInitializationService boardInitializationService,
            LinkDeadlockResolutionService deadlockResolutionService,
            LinkHintService hintService,
            LinkMatchCheckService matchCheckService
    ) {
        this.boardInitializationService = boardInitializationService;
        this.deadlockResolutionService = deadlockResolutionService;
        this.hintService = hintService;
        this.matchCheckService = matchCheckService;
    }

    /**
     * 创建一局新的游戏棋盘。
     */
    @PostMapping("/init")
    public ResponseEntity<GameBoardResponse> initGame(@RequestBody InitGameRequest request) {
        GameRuleConfig config = LinkDifficultyPresets.resolveRuleConfig(request == null ? null : request.getDifficulty());
        int[][] initialMap = boardInitializationService.initMap(config);
        if (initialMap == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(GameBoardResponse.of(false, "难度配置不合法", null, config));
        }

        int[][] resolvedMap = deadlockResolutionService.resolveDeadlock(initialMap, config);
        if (resolvedMap == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(GameBoardResponse.of(false, "棋盘初始化失败", null, config));
        }

        return ResponseEntity.ok(
                GameBoardResponse.of(true, "棋盘初始化成功", resolvedMap, config)
        );
    }

    /**
     * 仅重排当前仍然存在的图块，不回填已经被消除的空白格。
     */
    @PostMapping("/shuffle")
    public ResponseEntity<GameBoardResponse> shuffleGame(@RequestBody ShuffleMapRequest request) {
        if (request == null || request.getMap() == null || request.getConfig() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(GameBoardResponse.of(false, "请求参数不完整", null, null));
        }

        if (!LinkValidationUtils.isValidMap(request.getMap(), request.getConfig())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(GameBoardResponse.of(false, "地图格式不合法", null, request.getConfig()));
        }

        int[][] shuffledMap = boardInitializationService.shuffleMap(request.getMap(), request.getConfig());
        int[][] resolvedMap = deadlockResolutionService.resolveDeadlock(shuffledMap, request.getConfig());
        if (resolvedMap == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(GameBoardResponse.of(false, "地图格式不合法", null, request.getConfig()));
        }

        return ResponseEntity.ok(GameBoardResponse.of(true, "棋盘打乱成功", resolvedMap, request.getConfig()));
    }

    /**
     * 返回当前棋盘中的一组可消除图块，供前端做提示高亮。
     */
    @PostMapping("/hint")
    public ResponseEntity<HintResponse> getHint(@RequestBody ShuffleMapRequest request) {
        if (request == null || request.getMap() == null || request.getConfig() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(HintResponse.of(false, "请求参数不完整", null, null));
        }

        if (!LinkValidationUtils.isValidMap(request.getMap(), request.getConfig())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(HintResponse.of(false, "地图格式不合法", request.getConfig(), null));
        }

        List<Vertex> hintPair = hintService.findHintPair(request.getMap(), request.getConfig());
        if (hintPair.isEmpty()) {
            return ResponseEntity.ok(HintResponse.of(false, "当前棋盘没有可提示的组合", request.getConfig(), hintPair));
        }

        return ResponseEntity.ok(HintResponse.of(true, "已为你标出一组可消除图块", request.getConfig(), hintPair));
    }

    /**
     * 校验前端当前选中的两个格子是否符合连连看消除规则。
     */
    @PostMapping("/match-check")
    public ResponseEntity<MatchCheckResponse> checkMatch(@RequestBody MatchCheckRequest request) {
        if (request == null
                || request.getMap() == null
                || request.getConfig() == null
                || request.getV1() == null
                || request.getV2() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(MatchCheckResponse.of(false, "请求参数不完整", null, null, null));
        }

        MatchResult result = matchCheckService.checkMatch(
                request.getMap(),
                request.getV1(),
                request.getV2(),
                request.getConfig()
        );
        int[][] responseMap = request.getMap();
        String message = "当前两个方块无法连通";
        if (result.isConnected()) {
            if (deadlockResolutionService.isDeadlocked(responseMap, request.getConfig())) {
                int[][] resolvedMap = deadlockResolutionService.resolveDeadlock(responseMap, request.getConfig());
                if (resolvedMap == null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(MatchCheckResponse.of(false, "地图格式不合法", null, request.getConfig(), null));
                }
                responseMap = resolvedMap;
                message = "匹配成功，当前棋盘进入死局，已自动打乱";
            } else {
                message = "匹配成功";
            }
        }

        return ResponseEntity.ok(
                MatchCheckResponse.of(result.isConnected(), message, responseMap, request.getConfig(), result.getPath())
        );
    }
}
