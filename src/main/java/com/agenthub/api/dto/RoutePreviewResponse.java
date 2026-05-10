package com.agenthub.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoutePreviewResponse {
    private /** 路由策略标识 */ String routeId;
    private /** 模型提供方 */ String provider;
    private /** 模型名称 */ String model;
    private /** 调用端点 */ String endpoint;
    private /** 优先级 */ int priority;
}
