package com.agenthub.api.dto;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.application.dto.CitationOutput;
import com.agenthub.application.dto.RetrievalOutput;
import com.agenthub.application.dto.RetrievalResultOutput;

import java.util.List;

/**
 * 知识库检索响应。
 *
 * @param rewrittenQuery 改写后的查询文本
 * @param results        检索结果列表
 * @param citations      引用列表
 */
public record SearchResponse(
        String rewrittenQuery,
        List<RetrievalResultItem> results,
        List<CitationItem> citations
) {


    /**
     * 将流水线结果转换为搜索响应DTO。
     *
     * @param result 检索流水线结果
     * @return 搜索响应
     */
    public static SearchResponse toSearchResponse(RetrievalOutput result) {
        return new SearchResponse(
                result.getRewrittenQuery(),
                result.getResults().stream().map(SearchResponse::toResultItem).toList(),
                result.getCitations().stream().map(SearchResponse::toCitationItem).toList()
        );
    }

    /**
     * 将检索结果输出转换为结果项DTO。
     *
     * @param r 检索结果输出DTO
     * @return 检索结果项DTO
     */
    private static RetrievalResultItem toResultItem(RetrievalResultOutput r) {
        return BeanUtil.copyProperties(r, RetrievalResultItem.class);
    }

    /**
     * 将引用输出转换为引用项DTO。
     *
     * @param c 引用输出DTO
     * @return 引用项DTO
     */
    private static CitationItem toCitationItem(CitationOutput c) {
        return BeanUtil.copyProperties(c, CitationItem.class);
    }

}
