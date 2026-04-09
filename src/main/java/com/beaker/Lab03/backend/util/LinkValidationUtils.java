package com.beaker.Lab03.backend.util;

import com.beaker.Lab03.backend.model.pojo.Vertex;

/**
 * 欢乐连连看参数校验工具类。
 * 集中处理地图、坐标以及匹配请求的合法性判断，便于多个服务复用同一套规则。
 */
public final class LinkValidationUtils {

    /**
     * 校验棋盘结构是否合法。
     *
     * @param map 前端或服务端传入的棋盘二维数组
     * @return 合法返回 true，否则返回 false
     */
    public static boolean isValidMap(int[][] map) {
        if (map == null || map.length != LinkGameConstants.EXPECTED_ROWS) {
            return false;
        }

        for (int[] row : map) {
            if (row == null || row.length != LinkGameConstants.EXPECTED_COLS) {
                return false;
            }
        }
        return true;
    }

    /**
     * 校验坐标是否位于棋盘边界内。
     *
     * @param vertex 待校验的坐标点
     * @return 坐标合法返回 true，否则返回 false
     */
    public static boolean isCoordinateValid(Vertex vertex) {
        if (vertex == null) {
            return false;
        }

        int row = vertex.getRow();
        int col = vertex.getCol();
        return row >= 0
                && row < LinkGameConstants.EXPECTED_ROWS
                && col >= 0
                && col < LinkGameConstants.EXPECTED_COLS;
    }

    /**
     * 校验一次消除请求是否合法。
     * 规则包括：棋盘合法、两个点可参与匹配、不是同一个点、类型一致、并且坐标上的真实值与请求类型一致。
     *
     * @param map 当前棋盘
     * @param firstVertex 第一个坐标点
     * @param secondVertex 第二个坐标点
     * @return 合法返回 true，否则返回 false
     */
    public static boolean isValidMatchRequest(int[][] map, Vertex firstVertex, Vertex secondVertex) {
        if (!isValidMap(map)
                || !isCoordinateValid(firstVertex)
                || !isCoordinateValid(secondVertex)
                || map[firstVertex.getRow()][firstVertex.getCol()] == LinkGameConstants.BLANK
                || map[secondVertex.getRow()][secondVertex.getCol()] == LinkGameConstants.BLANK) {
            return false;
        }

        if (firstVertex.getRow() == secondVertex.getRow() && firstVertex.getCol() == secondVertex.getCol()) {
            return false;
        }

        int firstTypeOnMap = map[firstVertex.getRow()][firstVertex.getCol()];
        int secondTypeOnMap = map[secondVertex.getRow()][secondVertex.getCol()];

        return firstVertex.getType() == secondVertex.getType()
                && firstVertex.getType() == firstTypeOnMap
                && secondVertex.getType() == secondTypeOnMap;
    }
}
