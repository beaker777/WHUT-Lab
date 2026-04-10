package com.beaker.Lab03.backend.util;

import com.beaker.Lab03.backend.model.dto.GameRuleConfig;

/**
 * 欢乐连连看难度预设。
 * 集中维护各档位的棋盘尺寸、最大拐弯次数以及初始化筛选阈值。
 */
public final class LinkDifficultyPresets {

    private LinkDifficultyPresets() {
    }

    /**
     * 根据难度编码解析出规则配置。
     *
     * @param difficulty 难度编码
     * @return 对应规则；无法识别时返回默认普通难度
     */
    public static GameRuleConfig resolveRuleConfig(String difficulty) {
        String normalizedDifficulty = normalizeDifficulty(difficulty);
        if (LinkGameConstants.DIFFICULTY_EASY.equals(normalizedDifficulty)) {
            return new GameRuleConfig(
                    LinkGameConstants.DIFFICULTY_EASY,
                    LinkGameConstants.EASY_ROWS,
                    LinkGameConstants.EASY_COLS,
                    LinkGameConstants.EASY_MAX_TURNS
            );
        }
        if (LinkGameConstants.DIFFICULTY_HARD.equals(normalizedDifficulty)) {
            return new GameRuleConfig(
                    LinkGameConstants.DIFFICULTY_HARD,
                    LinkGameConstants.HARD_ROWS,
                    LinkGameConstants.HARD_COLS,
                    LinkGameConstants.HARD_MAX_TURNS
            );
        }
        return new GameRuleConfig(
                LinkGameConstants.DIFFICULTY_NORMAL,
                LinkGameConstants.DEFAULT_ROWS,
                LinkGameConstants.DEFAULT_COLS,
                LinkGameConstants.DEFAULT_MAX_TURNS
        );
    }

    /**
     * 判断规则配置是否合法。
     *
     * @param config 待校验配置
     * @return 合法返回 true，否则返回 false
     */
    public static boolean isRuleConfigValid(GameRuleConfig config) {
        if (config == null || config.getRows() <= 0 || config.getCols() <= 0 || config.getMaxTurns() < 0) {
            return false;
        }

        if ((config.getRows() * config.getCols()) % 2 != 0) {
            return false;
        }

        GameRuleConfig presetConfig = resolveRuleConfig(config.getDifficulty());
        return presetConfig.getRows() == config.getRows()
                && presetConfig.getCols() == config.getCols()
                && presetConfig.getMaxTurns() == config.getMaxTurns();
    }

    /**
     * 判断当前可消对子数量是否落在该难度的目标区间内。
     *
     * @param difficulty 难度编码
     * @param availablePairCount 当前可消对子数量
     * @return 落在目标区间返回 true，否则返回 false
     */
    public static boolean isPairCountSuitable(String difficulty, int availablePairCount) {
        String normalizedDifficulty = normalizeDifficulty(difficulty);
        if (LinkGameConstants.DIFFICULTY_EASY.equals(normalizedDifficulty)) {
            return availablePairCount >= LinkGameConstants.EASY_MIN_HINT_PAIR_COUNT;
        }
        if (LinkGameConstants.DIFFICULTY_HARD.equals(normalizedDifficulty)) {
            return availablePairCount >= LinkGameConstants.HARD_MIN_HINT_PAIR_COUNT
                    && availablePairCount <= LinkGameConstants.HARD_MAX_HINT_PAIR_COUNT;
        }
        return availablePairCount >= LinkGameConstants.NORMAL_MIN_HINT_PAIR_COUNT
                && availablePairCount <= LinkGameConstants.NORMAL_MAX_HINT_PAIR_COUNT;
    }

    /**
     * 返回该难度用于提前终止扫描的可消对子数量上限。
     *
     * @param difficulty 难度编码
     * @return 上限值
     */
    public static int getPairCountScanUpperBound(String difficulty) {
        String normalizedDifficulty = normalizeDifficulty(difficulty);
        if (LinkGameConstants.DIFFICULTY_EASY.equals(normalizedDifficulty)) {
            return LinkGameConstants.EASY_MIN_HINT_PAIR_COUNT;
        }
        if (LinkGameConstants.DIFFICULTY_HARD.equals(normalizedDifficulty)) {
            return LinkGameConstants.HARD_MAX_HINT_PAIR_COUNT + 1;
        }
        return LinkGameConstants.NORMAL_MAX_HINT_PAIR_COUNT + 1;
    }

    private static String normalizeDifficulty(String difficulty) {
        if (difficulty == null) {
            return LinkGameConstants.DIFFICULTY_NORMAL;
        }

        String upperDifficulty = difficulty.trim().toUpperCase();
        if (LinkGameConstants.DIFFICULTY_EASY.equals(upperDifficulty)
                || LinkGameConstants.DIFFICULTY_NORMAL.equals(upperDifficulty)
                || LinkGameConstants.DIFFICULTY_HARD.equals(upperDifficulty)) {
            return upperDifficulty;
        }
        return LinkGameConstants.DIFFICULTY_NORMAL;
    }
}
