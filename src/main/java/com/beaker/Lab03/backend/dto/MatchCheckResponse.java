package com.beaker.Lab03.backend.dto;

import com.beaker.Lab03.backend.pojo.Vertex;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 消除校验响应，包含是否连通、关键路径点和最新地图。
 */
@Data
public class MatchCheckResponse {

    private boolean connected;
    private String message;
    private int[][] map;
    private List<Vertex> path = Collections.emptyList();

    public static MatchCheckResponse of(boolean connected, String message, int[][] map, List<Vertex> path) {
        MatchCheckResponse response = new MatchCheckResponse();
        response.setConnected(connected);
        response.setMessage(message);
        response.setMap(map);
        response.setPath(path == null ? Collections.<Vertex>emptyList() : new ArrayList<Vertex>(path));
        return response;
    }

    public List<Vertex> getPath() {
        return new ArrayList<Vertex>(path);
    }

    public void setPath(List<Vertex> path) {
        this.path = new ArrayList<Vertex>(path);
    }
}
