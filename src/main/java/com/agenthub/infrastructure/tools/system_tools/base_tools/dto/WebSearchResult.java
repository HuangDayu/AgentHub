package com.agenthub.infrastructure.tools.system_tools.base_tools.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author huangdayu
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebSearchResult {
    private boolean success;
    private String query;
    private List<SearchItem> results;
    private int totalResults;
    private String message;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchItem {
        private String title;
        private String url;
        private String snippet;
    }


}

