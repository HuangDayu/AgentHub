package com.agenthub.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 市场搜索请求。
 */
@Data
@NoArgsConstructor
public class MarketSearchRequest {
    private String keyword;
    private String category;
    private String sortBy;
    private int page = 1;
    private int pageSize = 20;
}
