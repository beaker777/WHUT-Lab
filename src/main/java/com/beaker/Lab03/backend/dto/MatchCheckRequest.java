package com.beaker.Lab03.backend.dto;

import com.beaker.Lab03.backend.pojo.Vertex;
import lombok.Data;

/**
 * 前端发起消除校验请求的数据结构。
 */
@Data
public class MatchCheckRequest {

    /**
     * 连连看 map
     */
    private int[][] map;
    /**
     * 坐标 1
     */
    private Vertex v1;
    /**
     * 坐标 2
     */
    private Vertex v2;
}
