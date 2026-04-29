package com.agenthub.application.command;

import java.util.Map;

/**
 * 调用工具命令对象。
 * <p>
 * 封装调用工具所需的输入载荷和幂等键。
 *
 * @param idempotencyKey 幂等键，用于防止重复调用
 * @param payload        调用参数载荷
 */
public record InvokeToolCommand(
        /** 幂等键，用于防止重复调用 */
        String idempotencyKey,
        /** 调用参数载荷 */
        Map<String, Object> payload) {
}
