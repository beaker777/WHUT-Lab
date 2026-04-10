package com.beaker.Lab03.backend.model.dto;

import lombok.Data;

/**
 * 打乱当前棋盘请求。
 * 传入当前棋盘和本局规则，服务端负责保留空白格并重排剩余图块。
 */
@Data
public class ShuffleMapRequest {

    private int[][] map;
    private GameRuleConfig config;
}
