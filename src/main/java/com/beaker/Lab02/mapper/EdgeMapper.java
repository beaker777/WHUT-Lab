package com.beaker.Lab02.mapper;

import com.beaker.Lab02.pojo.Edge;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Author beaker
 * @Date 2026/3/17 15:13
 * @Description TODO
 */
@Mapper
public interface EdgeMapper {

    List<Edge> queryEdge();
}
