package com.agenthub.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 市场搜索条件。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarketSearchQuery {
    private String keyword;
    private String category;
    private String sortBy;
    private int page = 1;
    private int pageSize = 20;
}
