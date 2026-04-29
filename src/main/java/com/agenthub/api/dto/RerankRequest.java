package com.agenthub.api.dto;

import java.util.List;

/**
 * 重排请求记录。
 */
public record RerankRequest(
        String query,
        List<String> candidates
) {}
