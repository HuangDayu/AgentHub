package com.agenthub.application.command;

/** 创建工具命令对象。 */
public record CreateHttpToolCommand(
        /** 工具名称 */
        String name,
        /** 工具描述 */
        String description,
        /** 是否启用 */
        boolean enabled) {
}
