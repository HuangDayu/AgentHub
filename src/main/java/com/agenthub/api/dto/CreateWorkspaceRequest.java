package com.agenthub.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateWorkspaceRequest {
    private /** 工作空间编码 */ String workspaceCode;
    private /** 工作空间名称 */ String name;
    private /** 区域 */ String region;
}
