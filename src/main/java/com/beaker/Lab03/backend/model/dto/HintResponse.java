package com.beaker.Lab03.backend.model.dto;

import com.beaker.Lab03.backend.model.pojo.Vertex;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 提示接口响应。
 * 返回一组当前可以被消除的坐标点，供前端高亮展示。
 */
@Data
public class HintResponse {

    private boolean success;
    private String message;
    private GameRuleConfig config;
    private List<Vertex> hintTiles = Collections.emptyList();

    public static HintResponse of(boolean success, String message, GameRuleConfig config, List<Vertex> hintTiles) {
        HintResponse response = new HintResponse();
        response.setSuccess(success);
        response.setMessage(message);
        response.setConfig(config);
        response.setHintTiles(hintTiles == null ? Collections.<Vertex>emptyList() : new ArrayList<Vertex>(hintTiles));
        return response;
    }

    public List<Vertex> getHintTiles() {
        return new ArrayList<Vertex>(hintTiles);
    }

    public void setHintTiles(List<Vertex> hintTiles) {
        this.hintTiles = new ArrayList<Vertex>(hintTiles);
    }
}
