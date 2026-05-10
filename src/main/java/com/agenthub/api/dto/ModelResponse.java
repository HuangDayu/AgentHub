package com.agenthub.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModelResponse {
    private /** 模型提供方（openai / anthropic 等） */ String provider;
    private /** 模型名称 */ String model;
    private /** 模型返回的内容 */ String content;
    private /** 输入 token 数 */ int inputTokens;
    private /** 输出 token 数 */ int outputTokens;
    private /** 本次调用费用（美元） */ double cost;
}
