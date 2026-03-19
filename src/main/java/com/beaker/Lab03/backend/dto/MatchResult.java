package com.beaker.Lab03.backend.dto;

import com.beaker.Lab03.backend.pojo.Vertex;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 连连看匹配结果，包含是否成功以及前端绘线需要的关键路径点。
 */
@Data
public class MatchResult {

    private final boolean connected;
    private final String linkType;
    private final List<Vertex> path;

    private MatchResult(boolean connected, String linkType, List<Vertex> path) {
        this.connected = connected;
        this.linkType = linkType;
        this.path = path;
    }

    public static MatchResult success(String linkType, List<Vertex> path) {
        return new MatchResult(true, linkType, new ArrayList<Vertex>(path));
    }

    public static MatchResult failure() {
        return new MatchResult(false, "NONE", Collections.<Vertex>emptyList());
    }

    public List<Vertex> getPath() {
        return new ArrayList<Vertex>(path);
    }
}
