package com.agenthub.api.dto;

/**
 * 创建消息请求体。
 */
public record CreateMessageRequest(String role, String content) {
}
