package com.beaker.Lab03.backend.service;

import com.beaker.Lab03.backend.dto.MatchResult;
import com.beaker.Lab03.backend.pojo.Vertex;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 欢乐连连看核心连通算法：
 * 1. 直线连通
 * 2. 单拐点连通
 * 3. 双拐点连通
 */
@Service
public class LinkService {

    private static final int BLANK = 0;
    private static final int EXPECTED_ROWS = 10;
    private static final int EXPECTED_COLS = 16;
    private static final int ICON_TYPE_COUNT = 8;

    /**
     * 初始化一个 10x16 的新棋盘。
     */
    public int[][] initMap() {
        List<Integer> pairs = new ArrayList<Integer>();
        int pairCount = EXPECTED_ROWS * EXPECTED_COLS / 2;

        for (int index = 0; index < pairCount; index++) {
            int type = index % ICON_TYPE_COUNT + 1;
            pairs.add(type);
            pairs.add(type);
        }

        shufflePairs(pairs);
        return toMatrix(pairs);
    }

    /**
     * 打乱当前地图中的剩余图块，空白格保留为 0。
     */
    public int[][] shuffleMap(int[][] map) {
        if (!isValidMap(map)) {
            return null;
        }

        List<Integer> remainingTypes = new ArrayList<Integer>();
        for (int row = 0; row < EXPECTED_ROWS; row++) {
            for (int col = 0; col < EXPECTED_COLS; col++) {
                if (map[row][col] != BLANK) {
                    remainingTypes.add(map[row][col]);
                }
            }
        }

        shufflePairs(remainingTypes);

        int[][] shuffledMap = new int[EXPECTED_ROWS][EXPECTED_COLS];
        int valueIndex = 0;
        for (int row = 0; row < EXPECTED_ROWS; row++) {
            for (int col = 0; col < EXPECTED_COLS; col++) {
                if (map[row][col] == BLANK) {
                    shuffledMap[row][col] = BLANK;
                } else {
                    shuffledMap[row][col] = remainingTypes.get(valueIndex++);
                }
            }
        }

        return shuffledMap;
    }

    /**
     * 检查两个点是否可消除。
     * 若连通成功，会将地图中两个位置置为 0，并返回关键路径点。
     */
    public MatchResult checkMatch(int[][] map, Vertex v1, Vertex v2) {
        if (!isValidRequest(map, v1, v2)) {
            return MatchResult.failure();
        }

        MatchResult result = tryStraightLink(map, v1, v2);
        if (!result.isConnected()) {
            result = tryOneCornerLink(map, v1, v2);
        }
        if (!result.isConnected()) {
            result = tryTwoCornerLink(map, v1, v2);
        }

        if (result.isConnected()) {
            map[v1.getRow()][v1.getCol()] = BLANK;
            map[v2.getRow()][v2.getCol()] = BLANK;
        }

        return result;
    }

    private boolean isValidRequest(int[][] map, Vertex v1, Vertex v2) {
        if (!isValidMap(map)) {
            return false;
        }
        if (!isPlayableVertex(map, v1) || !isPlayableVertex(map, v2)) {
            return false;
        }
        if (v1.getRow() == v2.getRow() && v1.getCol() == v2.getCol()) {
            return false;
        }

        int sourceValue = map[v1.getRow()][v1.getCol()];
        int targetValue = map[v2.getRow()][v2.getCol()];

        if (sourceValue == BLANK || targetValue == BLANK) {
            return false;
        }
        if (sourceValue != targetValue) {
            return false;
        }

        return v1.getType() == sourceValue && v2.getType() == targetValue;
    }

    private boolean isValidMap(int[][] map) {
        if (map == null || map.length != EXPECTED_ROWS) {
            return false;
        }

        for (int[] row : map) {
            if (row == null || row.length != EXPECTED_COLS) {
                return false;
            }
        }
        return true;
    }

    private boolean isCoordinateValid(Vertex vertex) {
        if (vertex == null) {
            return false;
        }
        int row = vertex.getRow();
        if (row < 0 || row >= EXPECTED_ROWS) {
            return false;
        }
        int col = vertex.getCol();
        return col >= 0 && col < EXPECTED_COLS;
    }

    private boolean isPlayableVertex(int[][] map, Vertex vertex) {
        return isCoordinateValid(vertex) && map[vertex.getRow()][vertex.getCol()] != BLANK;
    }

    /**
     * 一条直线连通：仅允许横向或纵向。
     */
    private MatchResult tryStraightLink(int[][] map, Vertex v1, Vertex v2) {
        if (linkInRow(map, v1, v2) || linkInCol(map, v1, v2)) {
            return MatchResult.success("STRAIGHT", buildPath(v1, v2));
        }
        return MatchResult.failure();
    }

    private boolean linkInRow(int[][] map, Vertex v1, Vertex v2) {
        if (v1.getRow() != v2.getRow()) {
            return false;
        }

        int row = v1.getRow();
        int start = Math.min(v1.getCol(), v2.getCol()) + 1;
        int end = Math.max(v1.getCol(), v2.getCol()) - 1;

        for (int col = start; col <= end; col++) {
            if (map[row][col] != BLANK) {
                return false;
            }
        }

        return true;
    }

    private boolean linkInCol(int[][] map, Vertex v1, Vertex v2) {
        if (v1.getCol() != v2.getCol()) {
            return false;
        }

        int col = v1.getCol();
        int start = Math.min(v1.getRow(), v2.getRow()) + 1;
        int end = Math.max(v1.getRow(), v2.getRow()) - 1;

        for (int row = start; row <= end; row++) {
            if (map[row][col] != BLANK) {
                return false;
            }
        }

        return true;
    }

    /**
     * 两条直线连通：尝试两个候选直角拐点。
     */
    private MatchResult tryOneCornerLink(int[][] map, Vertex v1, Vertex v2) {
        Vertex corner1 = new Vertex(v1.getRow(), v2.getCol(), BLANK);
        if (isBlankPoint(map, corner1)
                && isDirectlyReachable(map, v1, corner1)
                && isDirectlyReachable(map, corner1, v2)) {
            return MatchResult.success("ONE_CORNER", buildPath(v1, corner1, v2));
        }

        Vertex corner2 = new Vertex(v2.getRow(), v1.getCol(), BLANK);
        if (isBlankPoint(map, corner2)
                && isDirectlyReachable(map, v1, corner2)
                && isDirectlyReachable(map, corner2, v2)) {
            return MatchResult.success("ONE_CORNER", buildPath(v1, corner2, v2));
        }

        return MatchResult.failure();
    }

    /**
     * 三条直线连通：从起点横向、纵向扫描所有可直达空白点，
     * 再判断这些空白点能否与终点形成单拐点连通。
     */
    private MatchResult tryTwoCornerLink(int[][] map, Vertex v1, Vertex v2) {
        MatchResult horizontalScanResult = scanRowForTwoCornerLink(map, v1, v2);
        if (horizontalScanResult.isConnected()) {
            return horizontalScanResult;
        }

        return scanColForTwoCornerLink(map, v1, v2);
    }

    private MatchResult scanRowForTwoCornerLink(int[][] map, Vertex v1, Vertex v2) {
        int row = v1.getRow();

        for (int col = v1.getCol() - 1; col >= 0; col--) {
            if (map[row][col] != BLANK) {
                break;
            }
            MatchResult result = tryOneCornerFromPivot(map, v1, v2, new Vertex(row, col, BLANK));
            if (result.isConnected()) {
                return result;
            }
        }

        for (int col = v1.getCol() + 1; col < map[row].length; col++) {
            if (map[row][col] != BLANK) {
                break;
            }
            MatchResult result = tryOneCornerFromPivot(map, v1, v2, new Vertex(row, col, BLANK));
            if (result.isConnected()) {
                return result;
            }
        }

        return MatchResult.failure();
    }

    private MatchResult scanColForTwoCornerLink(int[][] map, Vertex v1, Vertex v2) {
        int col = v1.getCol();

        for (int row = v1.getRow() - 1; row >= 0; row--) {
            if (map[row][col] != BLANK) {
                break;
            }
            MatchResult result = tryOneCornerFromPivot(map, v1, v2, new Vertex(row, col, BLANK));
            if (result.isConnected()) {
                return result;
            }
        }

        for (int row = v1.getRow() + 1; row < map.length; row++) {
            if (map[row][col] != BLANK) {
                break;
            }
            MatchResult result = tryOneCornerFromPivot(map, v1, v2, new Vertex(row, col, BLANK));
            if (result.isConnected()) {
                return result;
            }
        }

        return MatchResult.failure();
    }

    private MatchResult tryOneCornerFromPivot(int[][] map, Vertex start, Vertex end, Vertex pivot) {
        MatchResult pivotResult = tryOneCornerLink(map, pivot, end);
        if (!pivotResult.isConnected()) {
            return MatchResult.failure();
        }

        List<Vertex> finalPath = new ArrayList<Vertex>();
        finalPath.add(copyVertex(start));
        finalPath.add(copyVertex(pivot));

        List<Vertex> pivotPath = pivotResult.getPath();
        for (int i = 1; i < pivotPath.size(); i++) {
            finalPath.add(copyVertex(pivotPath.get(i)));
        }

        return MatchResult.success("TWO_CORNER", finalPath);
    }

    private boolean isDirectlyReachable(int[][] map, Vertex from, Vertex to) {
        return linkInRow(map, from, to) || linkInCol(map, from, to);
    }

    private boolean isBlankPoint(int[][] map, Vertex point) {
        return isCoordinateValid(point) && map[point.getRow()][point.getCol()] == BLANK;
    }

    private List<Vertex> buildPath(Vertex... vertices) {
        List<Vertex> path = new ArrayList<Vertex>();
        for (Vertex vertex : vertices) {
            path.add(copyVertex(vertex));
        }
        return path;
    }

    private Vertex copyVertex(Vertex vertex) {
        return new Vertex(vertex.getRow(), vertex.getCol(), vertex.getType());
    }

    private void shufflePairs(List<Integer> items) {
        Collections.shuffle(items, ThreadLocalRandom.current());
    }

    private int[][] toMatrix(List<Integer> values) {
        int[][] map = new int[EXPECTED_ROWS][EXPECTED_COLS];
        for (int index = 0; index < values.size(); index++) {
            int row = index / EXPECTED_COLS;
            int col = index % EXPECTED_COLS;
            map[row][col] = values.get(index);
        }
        return map;
    }
}
