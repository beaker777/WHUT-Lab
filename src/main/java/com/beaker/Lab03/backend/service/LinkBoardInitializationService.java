package com.beaker.Lab03.backend.service;

import com.beaker.Lab03.backend.model.dto.GameRuleConfig;
import com.beaker.Lab03.backend.util.LinkDifficultyPresets;
import com.beaker.Lab03.backend.util.LinkGameConstants;
import com.beaker.Lab03.backend.util.LinkValidationUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 欢乐连连看棋盘初始化服务。
 * 负责按难度规则生成新棋盘，并根据当前局规则对已有棋盘执行洗牌。
 */
@Service
public class LinkBoardInitializationService {

    private static final int MAX_INIT_ATTEMPTS = 300;

    @Resource
    private LinkHintService hintService;

    /**
     * 根据当前难度配置初始化一局新棋盘。
     * 生成后的棋盘必须至少存在可消对子，且可消对子数量要落在当前难度目标范围内。
     *
     * @param config 当前局规则配置
     * @return 新生成的棋盘；配置非法时返回 null
     */
    public int[][] initMap(GameRuleConfig config) {
        if (!LinkDifficultyPresets.isRuleConfigValid(config)) {
            return null;
        }

        int pairCount = config.getRows() * config.getCols() / 2;
        int hintPairUpperBound = LinkDifficultyPresets.getPairCountScanUpperBound(config.getDifficulty());
        for (int attempt = 0; attempt < MAX_INIT_ATTEMPTS; attempt++) {
            List<Integer> tilePairs = new ArrayList<Integer>();
            for (int index = 0; index < pairCount; index++) {
                int type = index % LinkGameConstants.ICON_TYPE_COUNT + 1;
                tilePairs.add(type);
                tilePairs.add(type);
            }

            shufflePairs(tilePairs);

            int[][] map = new int[config.getRows()][config.getCols()];
            for (int index = 0; index < tilePairs.size(); index++) {
                int row = index / config.getCols();
                int col = index % config.getCols();
                map[row][col] = tilePairs.get(index);
            }

            int availablePairCount = hintService.countConnectablePairs(map, config, hintPairUpperBound);
            if (LinkDifficultyPresets.isPairCountSuitable(config.getDifficulty(), availablePairCount)) {
                return map;
            }
        }

        return buildFallbackMap(config, pairCount);
    }

    /**
     * 打乱当前棋盘上剩余的非空白图块。
     * 已经被消除的位置继续保留为空白，不会在洗牌后重新填充。
     *
     * @param map 当前棋盘
     * @param config 当前局规则配置
     * @return 打乱后的新棋盘；若输入不合法则返回 null
     */
    public int[][] shuffleMap(int[][] map, GameRuleConfig config) {
        if (!LinkValidationUtils.isValidMap(map, config)) {
            return null;
        }

        List<Integer> remainingTypes = new ArrayList<Integer>();
        for (int row = 0; row < config.getRows(); row++) {
            for (int col = 0; col < config.getCols(); col++) {
                if (map[row][col] != LinkGameConstants.BLANK) {
                    remainingTypes.add(map[row][col]);
                }
            }
        }

        shufflePairs(remainingTypes);

        int[][] shuffledMap = new int[config.getRows()][config.getCols()];
        int valueIndex = 0;
        for (int row = 0; row < config.getRows(); row++) {
            for (int col = 0; col < config.getCols(); col++) {
                if (map[row][col] == LinkGameConstants.BLANK) {
                    shuffledMap[row][col] = LinkGameConstants.BLANK;
                } else {
                    shuffledMap[row][col] = remainingTypes.get(valueIndex++);
                }
            }
        }
        return shuffledMap;
    }

    /**
     * 使用 Fisher-Yates 算法原地打乱列表。
     *
     * @param items 待打乱的数据集合
     */
    private void shufflePairs(List<Integer> items) {
        for (int currentIndex = items.size() - 1; currentIndex > 0; currentIndex--) {
            int randomIndex = ThreadLocalRandom.current().nextInt(currentIndex + 1);
            int temp = items.get(currentIndex);
            items.set(currentIndex, items.get(randomIndex));
            items.set(randomIndex, temp);
        }
    }

    /**
     * 初始化尝试未命中目标区间时的兜底实现。
     * 兜底方案仍然保证棋盘尺寸合法，并至少生成一局可玩的结果。
     *
     * @param config 当前局规则配置
     * @param pairCount 图块对数量
     * @return 兜底棋盘
     */
    private int[][] buildFallbackMap(GameRuleConfig config, int pairCount) {
        List<Integer> tilePairs = new ArrayList<Integer>();
        for (int index = 0; index < pairCount; index++) {
            int type = index % LinkGameConstants.ICON_TYPE_COUNT + 1;
            tilePairs.add(type);
            tilePairs.add(type);
        }
        shufflePairs(tilePairs);

        int[][] fallbackMap = new int[config.getRows()][config.getCols()];
        for (int index = 0; index < tilePairs.size(); index++) {
            int row = index / config.getCols();
            int col = index % config.getCols();
            fallbackMap[row][col] = tilePairs.get(index);
        }
        return fallbackMap;
    }
}
