package com.agenthub.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoutePreviewRequest {
    private /** 租户标识 */ String tenantId;
    private /** 模型名称 */ String model;
}
