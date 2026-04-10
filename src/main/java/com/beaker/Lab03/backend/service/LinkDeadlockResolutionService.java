package com.beaker.Lab03.backend.service;

import com.beaker.Lab03.backend.model.dto.GameRuleConfig;
import com.beaker.Lab03.backend.util.LinkValidationUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 欢乐连连看死局检测与恢复服务。
 * 负责判断当前棋盘是否已经没有可消对子，并在必要时自动打乱剩余图块。
 */
@Service
public class LinkDeadlockResolutionService {

    private static final int MAX_SHUFFLE_ATTEMPTS = 20;

    @Resource
    private LinkBoardInitializationService boardInitializationService;
    @Resource
    private LinkHintService hintService;

    /**
     * 判断当前棋盘是否已经进入死局。
     *
     * @param map 当前棋盘
     * @param config 当前局规则配置
     * @return 已经死局返回 true，否则返回 false
     */
    public boolean isDeadlocked(int[][] map, GameRuleConfig config) {
        if (!LinkValidationUtils.isValidMap(map, config)) {
            return false;
        }

        return hintService.findHintPair(map, config).isEmpty();
    }

    /**
     * 在检测到死局时自动打乱棋盘，直到找到至少一组可消对子或达到最大尝试次数。
     * 如果当前棋盘本身可继续游戏，或棋盘已经清空，则直接返回原棋盘。
     *
     * @param map 当前棋盘
     * @param config 当前局规则配置
     * @return 处理后的棋盘；若输入非法则返回 null
     */
    public int[][] resolveDeadlock(int[][] map, GameRuleConfig config) {
        if (!LinkValidationUtils.isValidMap(map, config)) {
            return null;
        }

        if (!isDeadlocked(map, config)) {
            return map;
        }

        int[][] candidateMap = map;
        for (int attempt = 0; attempt < MAX_SHUFFLE_ATTEMPTS; attempt++) {
            candidateMap = boardInitializationService.shuffleMap(candidateMap, config);
            if (candidateMap == null || !isDeadlocked(candidateMap, config)) {
                return candidateMap;
            }
        }
        return candidateMap;
    }
}
