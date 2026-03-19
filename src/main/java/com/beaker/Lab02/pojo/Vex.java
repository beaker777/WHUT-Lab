package com.beaker.Lab02.pojo;

import lombok.Data;

/**
 * @Author beaker
 * @Date 2026/3/15 18:57
 * @Description TODO
 */
@Data
public class Vex {
    public int num;         // 景点编号
    public String name;     // 景点名字
    public String desc;     // 景点介绍

    public Vex(int num, String name, String desc) {
        this.num = num;
        this.name = name;
        this.desc = desc;
    }
}
