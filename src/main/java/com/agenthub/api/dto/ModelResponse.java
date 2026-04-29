package com.agenthub.api.dto;

/**
 * 模型调用响应值对象。
 *
 * @param provider     模型提供方（openai / anthropic 等）
 * @param model        模型名称
 * @param content      模型返回的内容
 * @param inputTokens  输入 token 数
 * @param outputTokens 输出 token 数
 * @param cost         本次调用费用（美元）
 */
public record ModelResponse(
        /** 模型提供方（openai / anthropic 等） */
        String provider,
        /** 模型名称 */
        String model,
        /** 模型返回的内容 */
        String content,
        /** 输入 token 数 */
        int inputTokens,
        /** 输出 token 数 */
        int outputTokens,
        /** 本次调用费用（美元） */
        double cost
) {
}
