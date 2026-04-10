package com.beaker.Lab03.backend.model.dto;

import com.beaker.Lab03.backend.model.pojo.Vertex;
import lombok.Data;

/**
 * 前端发起消除校验请求的数据结构。
 * 请求中会携带当前棋盘快照和两个候选坐标，服务端据此返回是否可消除。
 */
@Data
public class MatchCheckRequest {

    /**
     * 当前棋盘快照。0 表示该位置已为空。
     */
    private int[][] map;
    /**
     * 当前局规则配置。
     */
    private GameRuleConfig config;
    /**
     * 第一个候选方块坐标。
     */
    private Vertex v1;
    /**
     * 第二个候选方块坐标。
     */
    private Vertex v2;
}
