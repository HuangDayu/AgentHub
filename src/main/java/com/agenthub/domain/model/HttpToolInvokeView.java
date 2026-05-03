package com.agenthub.domain.model;

import java.util.Map;

/**
 * 工具调用结果视图对象。
 * <p>
 * 封装工具调用的结果信息，用于 API 响应。
 *
 * @param toolId 工具唯一标识
 * @param status 调用状态
 * @param output 调用输出数据
 */
public record HttpToolInvokeView(
        /** 工具唯一标识 */
        String toolId,
        /** 调用状态 */
        String status,
        /** 调用输出数据 */
        Map<String, Object> output) {
}
