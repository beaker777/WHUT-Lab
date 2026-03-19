package com.beaker.Lab02;

import com.beaker.Lab02.mapper.EdgeMapper;
import com.beaker.Lab02.mapper.VexMapper;
import com.beaker.Lab02.pojo.Edge;
import com.beaker.Lab02.pojo.Vex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;

@Service
public class TourismManager {
    private Graph graph;
    private Scanner scanner;

    @Autowired
    private VexMapper vexMapper;
    @Autowired
    private EdgeMapper edgeMapper;

    public TourismManager() {
        graph = new Graph();
        scanner = new Scanner(System.in);
    }

    // 任务2：从数据库读取并初始化图
    public void createGraph() {
        System.out.println("===== 创建景区景点图 =====");

        try {
            // 1. 从 MyBatis Mapper 获取数据
            List<Vex> vexes = vexMapper.queryVex();
            List<Edge> edges = edgeMapper.queryEdge();

            // 2. 遍历并插入景点 (Vex)
            for (Vex vex : vexes) {
                graph.insertVex(vex);
            }

            // 3. 遍历并插入道路边 (Edge)
            for (Edge edge : edges) {
                graph.insertEdge(edge);
            }

            // 4. 打印成功提示
            System.out.println("顶点数目: " + graph.getVexNum());
            System.out.println("从 MySQL 数据库加载并建图成功！");

        } catch (Exception e) {
            System.out.println("【数据库读取失败】请检查数据库连接或 SQL 语句是否正确。");
            e.printStackTrace();
        }
    }

    // 任务2：查询景点及其相邻路线
    public void getSpotInfo() {
        System.out.println("===== 查询景点信息 =====");
        if (graph.getVexNum() == 0) {
            System.out.println("请先执行操作1创建图！");
            return;
        }
        for (int i = 0; i < graph.getVexNum(); i++) {
            System.out.println(i + "-" + graph.getVex(i).name);
        }
        System.out.print("请输入想要查询的景点编号: ");
        int id = scanner.nextInt();
        if (id < 0 || id >= graph.getVexNum()) {
            System.out.println("编号无效！");
            return;
        }

        Vex v = graph.getVex(id);
        System.out.println(v.name);
        System.out.println(v.desc);
        System.out.println("------ 周边景区 ------");

        for (int i = 0; i < graph.getVexNum(); i++) {
            int w = graph.getWeight(id, i);
            if (w > 0 && w < Graph.INF) {
                System.out.println(v.name + " -> " + graph.getVex(i).name + " " + w + "m");
            }
        }
    }

    // 任务3：基于DFS的景点导航
    public void travelPath() {
        System.out.println("===== 旅游景点导航 =====");
        if (graph.getVexNum() == 0) {
            System.out.println("请先执行操作1创建图！");
            return;
        }
        for (int i = 0; i < graph.getVexNum(); i++) {
            System.out.println(i + "-" + graph.getVex(i).name);
        }
        System.out.print("请输入起始点编号: ");
        int startId = scanner.nextInt();

        List<List<Integer>> paths = graph.dfsTraverse(startId);
        System.out.println("导游路线为:");
        if (paths.isEmpty()) {
            System.out.println("未找到游览所有景点的路径。");
        } else {
            for (int i = 0; i < paths.size(); i++) {
                System.out.print("路线" + (i + 1) + ": ");
                List<Integer> path = paths.get(i);
                for (int j = 0; j < path.size(); j++) {
                    System.out.print(graph.getVex(path.get(j)).name + (j == path.size() - 1 ? "" : " -> "));
                }
                System.out.println();
            }
        }
    }

    // 任务4：最短路径
    public void findShortPath() {
        System.out.println("===== 搜索最短路径 =====");
        if (graph.getVexNum() == 0) {
            System.out.println("请先执行操作1创建图！");
            return;
        }
        for (int i = 0; i < graph.getVexNum(); i++) {
            System.out.println(i + "-" + graph.getVex(i).name);
        }
        System.out.print("请输入起点的编号: ");
        int start = scanner.nextInt();
        System.out.print("请输入终点的编号: ");
        int end = scanner.nextInt();

        Graph.ShortestPathResult result = graph.findShortestPaths(start, end);
        if (result.getDistance() == Graph.INF) {
            System.out.println("不可达！");
            return;
        }

        List<List<Integer>> paths = result.getPaths();
        System.out.println("最短距离为: " + result.getDistance());
        if (paths.isEmpty()) {
            System.out.println("未找到任何最短路径。");
            return;
        }

        for (int i = 0; i < paths.size(); i++) {
            List<Integer> path = paths.get(i);
            System.out.print("最短路线" + (i + 1) + ": ");
            for (int j = 0; j < path.size(); j++) {
                System.out.print(graph.getVex(path.get(j)).name + (j == path.size() - 1 ? "" : " -> "));
            }
            System.out.println();
        }
    }

    // 任务4：电路规划最小生成树
    public void designPath() {
        System.out.println("===== 铺设电路规划 =====");
        if (graph.getVexNum() == 0) {
            System.out.println("请先执行操作1创建图！");
            return;
        }
        System.out.println("在以下景点之间铺设电路:");
        graph.findMinTree();
    }
}
