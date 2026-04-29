package com.agenthub.application.command;

/**
 * 更新工具命令对象。
 * <p>
 * 封装更新工具所需的输入参数。
 *
 * @param name 工具名称
 * @param description 工具描述
 * @param enabled 工具是否启用
 */
public record UpdateToolCommand(
        /** 工具名称 */
        String name,
        /** 工具描述 */
        String description,
        /** 是否启用 */
        Boolean enabled) {
}
