package com.beaker.Lab03.backend.service;

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

import static com.beaker.Lab03.backend.util.LinkGameConstants.EXPECTED_COLS;
import static com.beaker.Lab03.backend.util.LinkGameConstants.EXPECTED_ROWS;

/**
 * 欢乐连连看匹配判定服务。
 * 使用 0-1 BFS 统一处理路径搜索，其中：
 * 1. 继续沿当前方向前进的代价为 0
 * 2. 改变方向发生拐弯的代价为 1
 * 3. 只要最少拐弯次数不超过 2，就认为两点可以消除
 */
@Service
public class LinkMatchCheckService {

    private static final int DIRECTION_COUNT = 4;
    private static final int MAX_TURN_COUNT = 2;
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
     * @return 匹配结果对象
     */
    public MatchResult checkMatch(int[][] map, Vertex firstVertex, Vertex secondVertex) {
        if (!LinkValidationUtils.isValidMatchRequest(map, firstVertex, secondVertex)) {
            return MatchResult.failure();
        }

        MatchResult result = findMatchByZeroOneBfs(map, firstVertex, secondVertex);
        if (result.isConnected()) {
            map[firstVertex.getRow()][firstVertex.getCol()] = LinkGameConstants.BLANK;
            map[secondVertex.getRow()][secondVertex.getCol()] = LinkGameConstants.BLANK;
        }
        return result;
    }

    /**
     * 使用 0-1 BFS 搜索起点到终点的最少拐点路径。
     *
     * @param map 当前棋盘
     * @param start 起点
     * @param end 终点
     * @return 搜索结果
     */
    private MatchResult findMatchByZeroOneBfs(int[][] map, Vertex start, Vertex end) {
        int[][] dist = new int[EXPECTED_ROWS][EXPECTED_COLS];
        int[][] prev = new int[EXPECTED_ROWS][EXPECTED_COLS];

        // 初始化 dist 和 prev 数组
        for (int row = 0; row < EXPECTED_ROWS; row++) {
            for (int col = 0; col < EXPECTED_COLS; col++) {
                dist[row][col] = INF;
                prev[row][col] = NO_DIRECTION;
            }
        }

        // 将起点放入队列
        dist[start.getRow()][start.getCol()] = 0;
        Deque<int[]> deque = new ArrayDeque<int[]>();
        deque.addFirst(new int[]{start.getRow(), start.getCol(), NO_DIRECTION});

        // 进行 bfs
        while (!deque.isEmpty()) {
            // 队列中第一个节点出队
            int[] currentState = deque.removeFirst();
            int currentRow = currentState[0];
            int currentCol = currentState[1];
            int currentDirection = currentState[2];
            int currentTurnCount = dist[currentRow][currentCol];

            for (int nextDirection = 0; nextDirection < DIRECTION_COUNT; nextDirection++) {
                int nextRow = currentRow + DIRECTION_STEPS[nextDirection][0];
                int nextCol = currentCol + DIRECTION_STEPS[nextDirection][1];

                // 如果 next 不可达, 跳过
                if (!canEnterCell(map, nextRow, nextCol, end)) {
                    continue;
                }
                // 如果 next 已经被走过, 跳过
                if (dist[nextRow][nextCol] != INF) {
                    continue;
                }

                // 如果当前方向不为 null, 且 next 的方向与当前方向不同, dist++
                int nextTurnCount = currentTurnCount;
                if (currentDirection != NO_DIRECTION && nextDirection != currentDirection) {
                    nextTurnCount++;
                }
                dist[nextRow][nextCol] = nextTurnCount;
                prev[nextRow][nextCol] = nextDirection;

                // 如果 next 的 dist 大于最大值, 不入队
                if (nextTurnCount > MAX_TURN_COUNT) {
                    continue;
                }

                // 如果当前节点是 end, 直接返回
                if (nextRow == end.getRow() && nextCol == end.getCol()) {
                    // 获取完整路径
                    List<Vertex> fullPath = buildFullPath(prev, start, end);
                    // 获取关键路径
                    List<Vertex> keyPath = compressToKeyPath(map, fullPath);

                    // 返回
                    return MatchResult.success(resolveLinkType(keyPath), keyPath);
                }


                // 增量为 0 入队首, 为 1 入队尾
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
     * @param prev 前驱方向表，保存“进入当前格子的方向”
     * @param start 起点
     * @param end 终点
     * @return 按移动顺序排列的完整路径
     */
    private List<Vertex> buildFullPath(int[][] prev, Vertex start, Vertex end) {
        List<Vertex> path = new ArrayList<Vertex>();
        int currentRow = end.getRow();
        int currentCol = end.getCol();

        // 反向回溯路径, 直到走到起点
        while (!(currentRow == start.getRow() && currentCol == start.getCol())) {
            // 将当前节点加入 path
            path.add(new Vertex(currentRow, currentCol, 0));

            // 更新当前节点
            int direction = prev[currentRow][currentCol];
            currentRow -= DIRECTION_STEPS[direction][0];
            currentCol -= DIRECTION_STEPS[direction][1];
        }

        // 将起点加入, 并反转 path
        path.add(new Vertex(start.getRow(), start.getCol(), start.getType()));
        Collections.reverse(path);
        return path;
    }

    /**
     * 将完整格子路径压缩为关键点路径，仅保留起点、拐点和终点。
     *
     * @param fullPath 完整路径
     * @return 压缩后的关键点路径
     */
    private List<Vertex> compressToKeyPath(int[][] map,List<Vertex> fullPath) {
        List<Vertex> keyPath = new ArrayList<Vertex>();

        // 先加入起点
        keyPath.add(copyVertex(map, fullPath.get(0)));

        for (int index = 1; index < fullPath.size() - 1; index++) {
            // 如果 prev 和 next 走的方向不一样, 说明 cur 为拐点
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
        if (keyPath.size() == 2) {
            return "STRAIGHT";
        }
        if (keyPath.size() == 3) {
            return "ONE_CORNER";
        }
        if (keyPath.size() == 4) {
            return "TWO_CORNER";
        }
        return "NONE";
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
        if (row < 0 || row >= EXPECTED_ROWS || col < 0 || col >= EXPECTED_COLS) {
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
     * @param vertex 原始坐标点
     * @return 复制后的坐标点
     */
    private Vertex copyVertex(int[][] map, Vertex vertex) {
        return new Vertex(vertex.getRow(), vertex.getCol(), map[vertex.getRow()][vertex.getCol()]);
    }

}
