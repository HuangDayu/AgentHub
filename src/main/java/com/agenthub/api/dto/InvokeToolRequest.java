package com.agenthub.api.dto;

import java.util.Map;

/**
 * 工具调用请求对象。
 * <p>
 * 封装调用工具所需的输入参数和幂等键。
 *
 * @param idempotencyKey 幂等键，用于防止重复调用
 * @param payload        调用参数载荷
 */
public record InvokeToolRequest(
        /** 幂等键，用于防止重复调用 */
        String idempotencyKey,
        /** 调用参数载荷 */
        Map<String, Object> payload) {
}
