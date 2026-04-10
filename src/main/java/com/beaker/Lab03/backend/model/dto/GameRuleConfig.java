package com.beaker.Lab03.backend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 单局游戏规则配置。
 * 用于描述当前棋盘的尺寸、难度和可接受的最大拐弯次数。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameRuleConfig {

    private String difficulty;
    private int rows;
    private int cols;
    private int maxTurns;
}
