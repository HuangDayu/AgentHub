package com.agenthub.api.dto;

import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvokeToolRequest {
    private /** 幂等键，用于防止重复调用 */ String idempotencyKey;
    private /** 调用参数载荷 */ Map<String, Object> payload;
}
