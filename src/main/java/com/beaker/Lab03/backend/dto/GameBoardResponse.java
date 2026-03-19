package com.beaker.Lab03.backend.dto;

import lombok.Data;

/**
 * 棋盘初始化/打乱响应。
 */
@Data
public class GameBoardResponse {

    private boolean success;
    private String message;
    private int[][] map;

    public static GameBoardResponse of(boolean success, String message, int[][] map) {
        GameBoardResponse response = new GameBoardResponse();
        response.setSuccess(success);
        response.setMessage(message);
        response.setMap(map);
        return response;
    }
}
