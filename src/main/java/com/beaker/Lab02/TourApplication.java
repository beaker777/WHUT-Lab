package com.beaker.Lab02;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.util.Scanner;

/**
 * @Author beaker
 * @Date 2026/3/15 19:02
 * @Description TODO
 */
@SpringBootApplication
public class TourApplication {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(TourApplication.class, args);
        TourismManager manager = context.getBean(TourismManager.class);

        Scanner scanner = new Scanner(System.in);
        boolean bRunning = true;

        while (bRunning) {
            System.out.println("\n===== 景区信息管理系统 =====");
            System.out.println("1. 创建景区景点图");
            System.out.println("2. 查询景点信息");
            System.out.println("3. 旅游景点导航");
            System.out.println("4. 搜索最短路径");
            System.out.println("5. 铺设电路规划");
            System.out.println("0. 退出");
            System.out.print("请输入操作编号<0~5>: ");

            int choice = -1;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (Exception e) {
                continue;
            }

            switch (choice) {
                case 1:
                    manager.createGraph();
                    break;
                case 2:
                    manager.getSpotInfo();
                    break;
                case 3:
                    manager.travelPath();
                    break;
                case 4:
                    manager.findShortPath();
                    break;
                case 5:
                    manager.designPath();
                    break;
                case 0:
                    bRunning = false;
                    System.out.println("系统已退出。");
                    break;
                default:
                    System.out.println("无效的输入，请重试！");
            }
        }
        scanner.close();
    }
}
