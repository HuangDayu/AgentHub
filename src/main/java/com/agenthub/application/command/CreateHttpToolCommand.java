package com.agenthub.application.command;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateHttpToolCommand {
    private /** 工具名称 */ String name;
    private /** 工具描述 */ String description;
    private /** 是否启用 */ boolean enabled;
}
