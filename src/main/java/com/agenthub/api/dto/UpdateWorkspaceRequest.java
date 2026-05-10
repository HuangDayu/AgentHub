package com.agenthub.api.dto;

import lombok.Getter;
import lombok.Setter;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateWorkspaceRequest {
    private String id;
    private /** 工作空间名称 */ String name;
    private /** 区域 */ String region;
}
