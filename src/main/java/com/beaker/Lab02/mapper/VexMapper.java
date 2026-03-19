package com.beaker.Lab02.mapper;

import com.beaker.Lab02.pojo.Vex;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Author beaker
 * @Date 2026/3/17 14:37
 * @Description TODO
 */
@Mapper
public interface VexMapper {

    List<Vex> queryVex();
}
