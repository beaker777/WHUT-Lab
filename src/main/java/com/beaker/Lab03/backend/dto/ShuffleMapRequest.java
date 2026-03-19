package com.beaker.Lab03.backend.dto;

import lombok.Data;

/**
 * 打乱当前棋盘请求。
 */
@Data
public class ShuffleMapRequest {

    private int[][] map;
}
