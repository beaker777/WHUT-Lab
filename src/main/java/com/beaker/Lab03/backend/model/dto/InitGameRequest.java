package com.beaker.Lab03.backend.model.dto;

import lombok.Data;

/**
 * 新开一局游戏请求。
 * 前端只需要传入难度档位，服务端会解析为本局完整规则配置。
 */
@Data
public class InitGameRequest {

    private String difficulty;
}
