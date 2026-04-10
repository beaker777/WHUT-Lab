package com.beaker.Lab03.backend.model.dto;

import lombok.Data;

/**
 * 棋盘初始化/打乱响应。
 * success 用于标记接口是否成功执行，message 用于直接展示给前端用户。
 */
@Data
public class GameBoardResponse {

    private boolean success;
    private String message;
    private int[][] map;
    private GameRuleConfig config;

    public static GameBoardResponse of(boolean success, String message, int[][] map, GameRuleConfig config) {
        GameBoardResponse response = new GameBoardResponse();
        response.setSuccess(success);
        response.setMessage(message);
        response.setMap(map);
        response.setConfig(config);
        return response;
    }
}
