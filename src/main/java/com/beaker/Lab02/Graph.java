package com.beaker.Lab02;

import com.beaker.Lab02.pojo.Edge;
import com.beaker.Lab02.pojo.Vex;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * @Author beaker
 * @Date 2026/3/15 18:57
 * @Description TODO
 */
public class Graph {
    private final int MAX_VERTEX_NUM = 20;
    private int[][] adjMatrix;       // 邻接矩阵
    private Vex[] vexs;              // 顶点信息数组
    private int vexNum;              // 当前图的顶点个数
    public static final int INF = Integer.MAX_VALUE; // 表示两点间没有直接道路

    public Graph() {
        adjMatrix = new int[MAX_VERTEX_NUM][MAX_VERTEX_NUM];
        vexs = new Vex[MAX_VERTEX_NUM];
        vexNum = 0;
        // 初始化权值矩阵，默认各点间不连通
        for (int i = 0; i < MAX_VERTEX_NUM; i++) {
            for (int j = 0; j < MAX_VERTEX_NUM; j++) {
                adjMatrix[i][j] = (i == j) ? 0 : INF;
            }
        }
    }

    // 插入顶点信息
    public boolean insertVex(Vex v) {
        if (vexNum >= MAX_VERTEX_NUM) return false;
        vexs[vexNum++] = v;
        return true;
    }

    // 插入边信息
    public boolean insertEdge(Edge e) {
        if (e.vex1 < 0 || e.vex1 >= vexNum || e.vex2 < 0 || e.vex2 >= vexNum) return false;
        adjMatrix[e.vex1][e.vex2] = e.weight;
        adjMatrix[e.vex2][e.vex1] = e.weight; // 无向图对称
        return true;
    }

    public int getVexNum() { return vexNum; }
    public Vex getVex(int v) { return vexs[v]; }
    public int getWeight(int v1, int v2) { return adjMatrix[v1][v2]; }

    // 任务3：使用深度优先搜索寻找游览所有景点的多条路线
    public List<List<Integer>> dfsTraverse(int startVex) {
        List<List<Integer>> allPaths = new ArrayList<>();
        boolean[] visited = new boolean[MAX_VERTEX_NUM];
        List<Integer> currentPath = new ArrayList<>();

        dfs(startVex, visited, currentPath, allPaths);
        return allPaths;
    }

    private void dfs(int current, boolean[] visited, List<Integer> currentPath, List<List<Integer>> allPaths) {
        visited[current] = true;    // 改为已访问
        currentPath.add(current);   // 记录路径

        // 若所有顶点都被访问过，则保存该条哈密顿路径
        if (currentPath.size() == vexNum) {
            allPaths.add(new ArrayList<>(currentPath));
        } else {
            // 搜索所有邻接点
            for (int i = 0; i < vexNum; i++) {
                if (adjMatrix[current][i] > 0 && adjMatrix[current][i] < INF && !visited[i]) {
                    dfs(i, visited, currentPath, allPaths); // 递归调用
                }
            }
        }

        // 回溯：恢复现场以便寻找下一条路径
        visited[current] = false;
        currentPath.remove(currentPath.size() - 1);
    }

    public ShortestPathResult findShortestPaths(int start, int end) {
        if (start < 0 || start >= vexNum || end < 0 || end >= vexNum) {
            return new ShortestPathResult(new ArrayList<>(), INF);
        }

        int[] dist = new int[vexNum];
        boolean[] visited = new boolean[vexNum];
        List<List<Integer>> parents = new ArrayList<>(vexNum);

        for (int i = 0; i < vexNum; i++) {
            dist[i] = INF;
            parents.add(new ArrayList<>());
        }

        dist[start] = 0;

        for (int i = 0; i < vexNum; i++) {
            int minDist = INF;
            int pos = -1;
            for (int j = 0; j < vexNum; j++) {
                if (!visited[j] && dist[j] < minDist) {
                    minDist = dist[j];
                    pos = j;
                }
            }
            if (pos == -1) break;

            visited[pos] = true;

            for (int v = 0; v < vexNum; v++) {
                int weight = adjMatrix[pos][v];
                if (!visited[v] && weight < INF) {
                    int alt = dist[pos] + weight;
                    if (alt < dist[v]) {
                        dist[v] = alt;
                        parents.get(v).clear();
                        parents.get(v).add(pos);
                    } else if (alt == dist[v]) {
                        parents.get(v).add(pos);
                    }
                }
            }
        }

        List<List<Integer>> paths = new ArrayList<>();
        if (dist[end] < INF) {
            collectPaths(end, start, parents, new ArrayDeque<>(), paths);
        }

        return new ShortestPathResult(paths, dist[end]);
    }

    private void collectPaths(int current, int start, List<List<Integer>> parents, Deque<Integer> path, List<List<Integer>> result) {
        path.addFirst(current);
        if (current == start) {
            result.add(new ArrayList<>(path));
        } else {
            for (int parent : parents.get(current)) {
                collectPaths(parent, start, parents, path, result);
            }
        }
        path.removeFirst();
    }

    public static class ShortestPathResult {
        private final List<List<Integer>> paths;
        private final int distance;

        public ShortestPathResult(List<List<Integer>> paths, int distance) {
            this.paths = paths;
            this.distance = distance;
        }

        public List<List<Integer>> getPaths() {
            return paths;
        }

        public int getDistance() {
            return distance;
        }
    }

    // 任务4：Prim算法构建最小生成树用于电路规划
    public void findMinTree() {
        int[] lowcost = new int[vexNum];
        int[] pre = new int[vexNum];

        for (int i = 1; i < vexNum; i++) {
            lowcost[i] = adjMatrix[0][i];
            pre[i] = 0;
        }

        int totalWeight = 0;

        for (int i = 1; i < vexNum; i++) {
            int min = INF;
            int pos = 0;
            for (int j = 1; j < vexNum; j++) {
                if (lowcost[j] != 0 && lowcost[j] < min) {
                    min = lowcost[j];
                    pos = j;
                }
            }

            System.out.printf("%s - %s \t%dm\n", vexs[pre[pos]].name, vexs[pos].name, min);
            totalWeight += min;
            lowcost[pos] = 0;

            for (int j = 1; j < vexNum; j++) {
                if (lowcost[j] != 0 && adjMatrix[pos][j] < lowcost[j]) {
                    lowcost[j] = adjMatrix[pos][j];
                    pre[j] = pos;
                }
            }
        }
        System.out.println("铺设电路的总长度为: " + totalWeight);
    }
}
