package com.beaker.Lab03.backend.service;

import com.beaker.Lab03.backend.model.pojo.Vertex;
import com.beaker.Lab03.backend.util.LinkGameConstants;
import com.beaker.Lab03.backend.util.LinkValidationUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.beaker.Lab03.backend.util.LinkGameConstants.*;

/**
 * 欢乐连连看提示服务。
 * 负责从当前棋盘中找出一组可以消除的图块，供前端提示玩家下一步操作。
 */
@Service
public class LinkHintService {

    @Resource
    private LinkMatchCheckService matchCheckService;

    /**
     * 在当前棋盘中搜索第一组可消除图块。
     *
     * @param map 当前棋盘
     * @return 找到提示时返回两个坐标点，否则返回空集合
     */
    public List<Vertex> findHintPair(int[][] map) {
        if (!LinkValidationUtils.isValidMap(map)) {
            return Collections.emptyList();
        }

        // 初始化分类链表
        List<List<Vertex>> groupedVertices = new ArrayList<List<Vertex>>();
        for (int type = 0; type <= ICON_TYPE_COUNT; type++) {
            groupedVertices.add(new ArrayList<Vertex>());
        }

        // 填充分类链表
        for (int row = 0; row < EXPECTED_ROWS; row++) {
            for (int col = 0; col < EXPECTED_COLS; col++) {
                int tileType = map[row][col];
                if (tileType == BLANK) {
                    continue;
                }

                groupedVertices.get(tileType).add(new Vertex(row, col, tileType));
            }
        }

        // 校验是否存在两个可以被消除的方块
        for (int type = 1; type <= ICON_TYPE_COUNT; type++) {
            List<Vertex> sameTypeVertices = groupedVertices.get(type);
            for (int firstIndex = 0; firstIndex < sameTypeVertices.size() - 1; firstIndex++) {
                Vertex firstVertex = sameTypeVertices.get(firstIndex);
                for (int secondIndex = firstIndex + 1; secondIndex < sameTypeVertices.size(); secondIndex++) {
                    Vertex secondVertex = sameTypeVertices.get(secondIndex);

                    // 找到后返回
                    if (matchCheckService.canConnect(map, firstVertex, secondVertex)) {
                        List<Vertex> hintPair = new ArrayList<Vertex>();
                        hintPair.add(new Vertex(firstVertex.getRow(), firstVertex.getCol(), firstVertex.getType()));
                        hintPair.add(new Vertex(secondVertex.getRow(), secondVertex.getCol(), secondVertex.getType()));
                        return hintPair;
                    }
                }
            }
        }

        // 没找到, 返回空
        return Collections.emptyList();
    }
}
