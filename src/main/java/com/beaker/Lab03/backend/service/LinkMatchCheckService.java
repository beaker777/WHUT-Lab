package com.beaker.Lab03.backend.service;

import com.beaker.Lab03.backend.model.dto.GameRuleConfig;
import com.beaker.Lab03.backend.model.dto.MatchResult;
import com.beaker.Lab03.backend.model.pojo.Vertex;
import com.beaker.Lab03.backend.util.LinkGameConstants;
import com.beaker.Lab03.backend.util.LinkValidationUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

/**
 * 欢乐连连看匹配判定服务。
 * 使用 0-1 BFS 统一处理路径搜索，其中继续沿当前方向前进代价为 0，转向代价为 1。
 */
@Service
public class LinkMatchCheckService {

    private static final int DIRECTION_COUNT = 4;
    private static final int NO_DIRECTION = -1;
    private static final int[][] DIRECTION_STEPS = {
            {-1, 0},
            {0, 1},
            {1, 0},
            {0, -1}
    };
    private static final int INF = Integer.MAX_VALUE;

    /**
     * 检查两个点是否能够按连连看规则连通。
     * 如果匹配成功，会将这两个位置置为空白，并返回供前端绘线使用的关键路径点集合。
     *
     * @param map 当前棋盘
     * @param firstVertex 第一个方块坐标
     * @param secondVertex 第二个方块坐标
     * @param config 当前局规则配置
     * @return 匹配结果对象
     */
    public MatchResult checkMatch(int[][] map, Vertex firstVertex, Vertex secondVertex, GameRuleConfig config) {
        if (!LinkValidationUtils.isValidMatchRequest(map, firstVertex, secondVertex, config)) {
            return MatchResult.failure();
        }

        MatchResult result = findMatchByZeroOneBfs(map, firstVertex, secondVertex, true, config);
        if (result.isConnected()) {
            map[firstVertex.getRow()][firstVertex.getCol()] = LinkGameConstants.BLANK;
            map[secondVertex.getRow()][secondVertex.getCol()] = LinkGameConstants.BLANK;
        }
        return result;
    }

    /**
     * 仅判断两个点当前是否可以连通，不修改棋盘内容。
     *
     * @param map 当前棋盘
     * @param firstVertex 第一个方块坐标
     * @param secondVertex 第二个方块坐标
     * @param config 当前局规则配置
     * @return 可以连通返回 true，否则返回 false
     */
    public boolean canConnect(int[][] map, Vertex firstVertex, Vertex secondVertex, GameRuleConfig config) {
        if (!LinkValidationUtils.isValidMatchRequest(map, firstVertex, secondVertex, config)) {
            return false;
        }

        return findMatchByZeroOneBfs(map, firstVertex, secondVertex, false, config).isConnected();
    }

    /**
     * 使用 0-1 BFS 搜索起点到终点的最少拐点路径。
     *
     * @param map 当前棋盘
     * @param start 起点
     * @param end 终点
     * @param needPath 是否需要构建绘线路径
     * @param config 当前局规则配置
     * @return 搜索结果
     */
    private MatchResult findMatchByZeroOneBfs(
            int[][] map,
            Vertex start,
            Vertex end,
            boolean needPath,
            GameRuleConfig config
    ) {
        int rowCount = config.getRows();
        int colCount = config.getCols();
        int[][] dist = new int[rowCount][colCount];
        int[][] prev = new int[rowCount][colCount];

        for (int row = 0; row < rowCount; row++) {
            for (int col = 0; col < colCount; col++) {
                dist[row][col] = INF;
                prev[row][col] = NO_DIRECTION;
            }
        }

        dist[start.getRow()][start.getCol()] = 0;
        Deque<int[]> deque = new ArrayDeque<int[]>();
        deque.addFirst(new int[]{start.getRow(), start.getCol(), NO_DIRECTION});

        while (!deque.isEmpty()) {
            int[] currentState = deque.removeFirst();
            int currentRow = currentState[0];
            int currentCol = currentState[1];
            int currentDirection = currentState[2];
            int currentTurnCount = dist[currentRow][currentCol];

            for (int nextDirection = 0; nextDirection < DIRECTION_COUNT; nextDirection++) {
                int nextRow = currentRow + DIRECTION_STEPS[nextDirection][0];
                int nextCol = currentCol + DIRECTION_STEPS[nextDirection][1];
                if (!canEnterCell(map, nextRow, nextCol, end)) {
                    continue;
                }
                if (dist[nextRow][nextCol] != INF) {
                    continue;
                }

                int nextTurnCount = currentTurnCount;
                if (currentDirection != NO_DIRECTION && nextDirection != currentDirection) {
                    nextTurnCount++;
                }
                dist[nextRow][nextCol] = nextTurnCount;
                prev[nextRow][nextCol] = nextDirection;

                if (nextTurnCount > config.getMaxTurns()) {
                    continue;
                }

                if (nextRow == end.getRow() && nextCol == end.getCol()) {
                    if (!needPath) {
                        return MatchResult.success("NONE", Collections.<Vertex>emptyList());
                    }

                    List<Vertex> fullPath = buildFullPath(prev, start, end);
                    List<Vertex> keyPath = compressToKeyPath(map, fullPath);
                    return MatchResult.success(resolveLinkType(keyPath), keyPath);
                }

                if (currentDirection == NO_DIRECTION || nextDirection == currentDirection) {
                    deque.addFirst(new int[]{nextRow, nextCol, nextDirection});
                } else {
                    deque.addLast(new int[]{nextRow, nextCol, nextDirection});
                }
            }
        }

        return MatchResult.failure();
    }

    /**
     * 构造从起点到终点的完整格子路径。
     *
     * @param prev 前驱方向表，保存进入当前格子的方向
     * @param start 起点
     * @param end 终点
     * @return 按移动顺序排列的完整路径
     */
    private List<Vertex> buildFullPath(int[][] prev, Vertex start, Vertex end) {
        List<Vertex> path = new ArrayList<Vertex>();
        int currentRow = end.getRow();
        int currentCol = end.getCol();

        while (!(currentRow == start.getRow() && currentCol == start.getCol())) {
            path.add(new Vertex(currentRow, currentCol, 0));
            int direction = prev[currentRow][currentCol];
            currentRow -= DIRECTION_STEPS[direction][0];
            currentCol -= DIRECTION_STEPS[direction][1];
        }

        path.add(new Vertex(start.getRow(), start.getCol(), start.getType()));
        Collections.reverse(path);
        return path;
    }

    /**
     * 将完整格子路径压缩为关键点路径，仅保留起点、拐点和终点。
     *
     * @param map 当前棋盘
     * @param fullPath 完整路径
     * @return 压缩后的关键点路径
     */
    private List<Vertex> compressToKeyPath(int[][] map, List<Vertex> fullPath) {
        List<Vertex> keyPath = new ArrayList<Vertex>();
        keyPath.add(copyVertex(map, fullPath.get(0)));

        for (int index = 1; index < fullPath.size() - 1; index++) {
            Vertex previousPoint = fullPath.get(index - 1);
            Vertex currentPoint = fullPath.get(index);
            Vertex nextPoint = fullPath.get(index + 1);
            int previousRowStep = currentPoint.getRow() - previousPoint.getRow();
            int previousColStep = currentPoint.getCol() - previousPoint.getCol();
            int nextRowStep = nextPoint.getRow() - currentPoint.getRow();
            int nextColStep = nextPoint.getCol() - currentPoint.getCol();
            if (previousRowStep != nextRowStep || previousColStep != nextColStep) {
                keyPath.add(copyVertex(map, currentPoint));
            }
        }

        keyPath.add(copyVertex(map, fullPath.get(fullPath.size() - 1)));
        return keyPath;
    }

    /**
     * 根据关键点数量推断路径类型。
     *
     * @param keyPath 关键点路径
     * @return 连线类型
     */
    private String resolveLinkType(List<Vertex> keyPath) {
        int cornerCount = Math.max(0, keyPath.size() - 2);
        if (cornerCount == 0) {
            return "STRAIGHT";
        }
        if (cornerCount == 1) {
            return "ONE_CORNER";
        }
        if (cornerCount == 2) {
            return "TWO_CORNER";
        }
        if (cornerCount == 3) {
            return "THREE_CORNER";
        }
        return "MULTI_CORNER";
    }

    /**
     * 判断下一个格子是否允许进入。
     * 普通情况下只能走空白格，但终点虽然非空，也允许作为可进入节点。
     *
     * @param map 当前棋盘
     * @param row 待进入格子的行坐标
     * @param col 待进入格子的列坐标
     * @param end 终点
     * @return 可进入返回 true，否则返回 false
     */
    private boolean canEnterCell(int[][] map, int row, int col, Vertex end) {
        if (row < 0 || row >= map.length || col < 0 || col >= map[0].length) {
            return false;
        }

        if (row == end.getRow() && col == end.getCol()) {
            return true;
        }

        return map[row][col] == LinkGameConstants.BLANK;
    }

    /**
     * 复制一个坐标点，避免将内部对象直接暴露给外部路径结果。
     *
     * @param map 当前棋盘
     * @param vertex 原始坐标点
     * @return 复制后的坐标点
     */
    private Vertex copyVertex(int[][] map, Vertex vertex) {
        return new Vertex(vertex.getRow(), vertex.getCol(), map[vertex.getRow()][vertex.getCol()]);
    }
}
