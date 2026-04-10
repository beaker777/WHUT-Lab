package com.beaker.Lab03.backend.service;

import com.beaker.Lab03.backend.util.LinkGameConstants;
import com.beaker.Lab03.backend.util.LinkValidationUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 欢乐连连看棋盘初始化服务。
 * 负责新棋盘生成和现有棋盘洗牌，不参与具体的连通性判定。
 */
@Service
public class LinkBoardInitializationService {

    /**
     * 初始化一个固定尺寸的新棋盘。
     * 每种图块都按成对形式生成，确保基础数据满足消除类游戏的配对要求。
     *
     * @return 新生成的棋盘
     */
    public int[][] initMap() {
        List<Integer> tilePairs = new ArrayList<Integer>();
        int pairCount = LinkGameConstants.EXPECTED_ROWS * LinkGameConstants.EXPECTED_COLS / 2;
        for (int index = 0; index < pairCount; index++) {
            int type = index % LinkGameConstants.ICON_TYPE_COUNT + 1;
            tilePairs.add(type);
            tilePairs.add(type);
        }

        shufflePairs(tilePairs);

        int[][] map = new int[LinkGameConstants.EXPECTED_ROWS][LinkGameConstants.EXPECTED_COLS];
        for (int index = 0; index < tilePairs.size(); index++) {
            int row = index / LinkGameConstants.EXPECTED_COLS;
            int col = index % LinkGameConstants.EXPECTED_COLS;
            map[row][col] = tilePairs.get(index);
        }
        return map;
    }

    /**
     * 打乱当前棋盘上剩余的非空白图块。
     * 已经被消除的位置继续保留为空白，不会在洗牌后重新填充。
     *
     * @param map 当前棋盘
     * @return 打乱后的新棋盘；若输入不合法则返回 null
     */
    public int[][] shuffleMap(int[][] map) {
        if (!LinkValidationUtils.isValidMap(map)) {
            return null;
        }

        List<Integer> remainingTypes = new ArrayList<Integer>();
        for (int row = 0; row < LinkGameConstants.EXPECTED_ROWS; row++) {
            for (int col = 0; col < LinkGameConstants.EXPECTED_COLS; col++) {
                if (map[row][col] != LinkGameConstants.BLANK) {
                    remainingTypes.add(map[row][col]);
                }
            }
        }

        shufflePairs(remainingTypes);

        int[][] shuffledMap = new int[LinkGameConstants.EXPECTED_ROWS][LinkGameConstants.EXPECTED_COLS];
        int valueIndex = 0;
        for (int row = 0; row < LinkGameConstants.EXPECTED_ROWS; row++) {
            for (int col = 0; col < LinkGameConstants.EXPECTED_COLS; col++) {
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

    private void shufflePairsDeadTest(List<Integer> items) {
        int expectedSize = LinkGameConstants.EXPECTED_ROWS * LinkGameConstants.EXPECTED_COLS;
        if (items == null || items.size() != expectedSize) {
            shufflePairs(items);
            return;
        }

        /*
         * 该测试布局只服务于死局检测验证：
         * 1. 棋盘初始仅保留一对可直接消除的图块 (0,0) 和 (0,1)
         * 2. 额外保留两个无法继续配对的残留图块，确保消除这一对后棋盘立即进入死局
         * 3. 其余位置全部置为空白，便于直接观察“消除一次 -> 死局 -> 触发打乱”的流程
         */
        for (int row = 0; row < LinkGameConstants.EXPECTED_ROWS; row++) {
            for (int col = 0; col < LinkGameConstants.EXPECTED_COLS; col++) {
                items.set(row * LinkGameConstants.EXPECTED_COLS + col, LinkGameConstants.BLANK);
            }
        }

        // 唯一的可消对子。
        items.set(0, 1);
        items.set(1, 1);
        // 保留两个互不配对的残留图块，消除一次后立即进入死局。
        items.set(LinkGameConstants.EXPECTED_COLS + 2, 2);
        items.set(LinkGameConstants.EXPECTED_COLS * 2 + 3, 3);
    }
}
