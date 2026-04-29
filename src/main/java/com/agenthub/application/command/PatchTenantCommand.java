package com.agenthub.application.command;

/**
 * 更新租户命令.
 * <p>
 * 封装部分更新租户所需的字段。
 * </p>
 *
 * @param name 租户名称（可选）
 */
public record PatchTenantCommand(
        /** 租户名称（可选） */ String name
) {
}
