package com.beaker.Lab02.pojo;

import lombok.Data;

/**
 * @Author beaker
 * @Date 2026/3/15 18:58
 * @Description TODO
 */
@Data
public class Edge {
    public int vex1;        // 边的第一个顶点
    public int vex2;        // 边的第二个顶点
    public int weight;      // 权值（距离）

    public Edge(int vex1, int vex2, int weight) {
        this.vex1 = vex1;
        this.vex2 = vex2;
        this.weight = weight;
    }
}
