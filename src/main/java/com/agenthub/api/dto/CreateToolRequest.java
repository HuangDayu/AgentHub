package com.agenthub.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateToolRequest {
    private /** 工具名称 */ String name;
    private /** 工具描述 */ String description;
    private /** 是否启用 */ Boolean enabled;
}
