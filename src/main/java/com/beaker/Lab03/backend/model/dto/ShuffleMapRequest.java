package com.beaker.Lab03.backend.model.dto;

import lombok.Data;

/**
 * 打乱当前棋盘请求。
 * 只传当前棋盘即可，服务端负责保留空白格并重排剩余图块。
 */
@Data
public class ShuffleMapRequest {

    private int[][] map;
}
