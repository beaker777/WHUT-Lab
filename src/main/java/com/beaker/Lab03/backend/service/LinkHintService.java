package com.beaker.Lab03.backend.service;

import com.beaker.Lab03.backend.model.dto.GameRuleConfig;
import com.beaker.Lab03.backend.model.pojo.Vertex;
import com.beaker.Lab03.backend.util.LinkGameConstants;
import com.beaker.Lab03.backend.util.LinkValidationUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 欢乐连连看提示服务。
 * 负责从当前棋盘中找出一组可以消除的图块，并统计当前局面的可消对子数量。
 */
@Service
public class LinkHintService {

    @Resource
    private LinkMatchCheckService matchCheckService;

    /**
     * 在当前棋盘中搜索第一组可消除图块。
     *
     * @param map 当前棋盘
     * @param config 当前局规则配置
     * @return 找到提示时返回两个坐标点，否则返回空集合
     */
    public List<Vertex> findHintPair(int[][] map, GameRuleConfig config) {
        if (!LinkValidationUtils.isValidMap(map, config)) {
            return Collections.emptyList();
        }

        List<List<Vertex>> groupedVertices = buildGroupedVertices(map, config);
        for (int type = 1; type <= LinkGameConstants.ICON_TYPE_COUNT; type++) {
            List<Vertex> sameTypeVertices = groupedVertices.get(type);
            for (int firstIndex = 0; firstIndex < sameTypeVertices.size() - 1; firstIndex++) {
                Vertex firstVertex = sameTypeVertices.get(firstIndex);
                for (int secondIndex = firstIndex + 1; secondIndex < sameTypeVertices.size(); secondIndex++) {
                    Vertex secondVertex = sameTypeVertices.get(secondIndex);
                    if (matchCheckService.canConnect(map, firstVertex, secondVertex, config)) {
                        List<Vertex> hintPair = new ArrayList<Vertex>();
                        hintPair.add(new Vertex(firstVertex.getRow(), firstVertex.getCol(), firstVertex.getType()));
                        hintPair.add(new Vertex(secondVertex.getRow(), secondVertex.getCol(), secondVertex.getType()));
                        return hintPair;
                    }
                }
            }
        }
        return Collections.emptyList();
    }

    /**
     * 统计当前棋盘中可消对子数量。
     * 为了避免初始化阶段扫描过重，可通过 stopAfter 参数提前终止。
     *
     * @param map 当前棋盘
     * @param config 当前局规则配置
     * @param stopAfter 达到该数量后立即返回
     * @return 当前已统计到的可消对子数量
     */
    public int countConnectablePairs(int[][] map, GameRuleConfig config, int stopAfter) {
        if (!LinkValidationUtils.isValidMap(map, config)) {
            return 0;
        }

        int connectablePairCount = 0;
        List<List<Vertex>> groupedVertices = buildGroupedVertices(map, config);
        for (int type = 1; type <= LinkGameConstants.ICON_TYPE_COUNT; type++) {
            List<Vertex> sameTypeVertices = groupedVertices.get(type);
            for (int firstIndex = 0; firstIndex < sameTypeVertices.size() - 1; firstIndex++) {
                Vertex firstVertex = sameTypeVertices.get(firstIndex);
                for (int secondIndex = firstIndex + 1; secondIndex < sameTypeVertices.size(); secondIndex++) {
                    if (matchCheckService.canConnect(map, firstVertex, sameTypeVertices.get(secondIndex), config)) {
                        connectablePairCount++;
                        if (connectablePairCount >= stopAfter) {
                            return connectablePairCount;
                        }
                    }
                }
            }
        }
        return connectablePairCount;
    }

    /**
     * 将棋盘中的非空图块按类型分组，便于后续扫描提示或统计可消对子数量。
     *
     * @param map 当前棋盘
     * @param config 当前局规则配置
     * @return 按图块类型分组后的坐标集合
     */
    private List<List<Vertex>> buildGroupedVertices(int[][] map, GameRuleConfig config) {
        List<List<Vertex>> groupedVertices = new ArrayList<List<Vertex>>();
        for (int type = 0; type <= LinkGameConstants.ICON_TYPE_COUNT; type++) {
            groupedVertices.add(new ArrayList<Vertex>());
        }

        for (int row = 0; row < config.getRows(); row++) {
            for (int col = 0; col < config.getCols(); col++) {
                int tileType = map[row][col];
                if (tileType == LinkGameConstants.BLANK) {
                    continue;
                }
                if (tileType < 1 || tileType > LinkGameConstants.ICON_TYPE_COUNT) {
                    return Collections.emptyList();
                }

                groupedVertices.get(tileType).add(new Vertex(row, col, tileType));
            }
        }
        return groupedVertices;
    }
}
