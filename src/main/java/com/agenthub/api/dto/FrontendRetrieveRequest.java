package com.agenthub.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 前端 Tenant Console 知识检索请求。
 * <p>
 * kbCode 通过 body 传递（而非路径变量），与前端 API 契约一致。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FrontendRetrieveRequest {
    private String kbId;
    private String query;
    private Integer topK = 5;
}
