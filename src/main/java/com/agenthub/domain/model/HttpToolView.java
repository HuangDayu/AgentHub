package com.agenthub.domain.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HttpToolView {
    private /** 工具唯一标识 */ String id;
    private /** 工具名称 */ String name;
    private /** 工具描述 */ String description;
    private /** 是否启用 */ boolean enabled;
}
