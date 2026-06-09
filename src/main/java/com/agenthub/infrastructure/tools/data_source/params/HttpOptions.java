package com.agenthub.infrastructure.tools.data_source.params;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * HTTP 请求选项 DTO
 */
@Data
@NoArgsConstructor
public class HttpOptions {

    /** 请求路径（追加到URI后） */
    private String path;

    /** 请求体JSON字符串 */
    private String body;

    /** 查询参数JSON字符串 */
    private String queryParams;
}
