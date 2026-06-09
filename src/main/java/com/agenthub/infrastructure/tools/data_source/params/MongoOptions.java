package com.agenthub.infrastructure.tools.data_source.params;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MongoDB 操作选项 DTO
 */
@Data
@NoArgsConstructor
public class MongoOptions {

    /** 查询条件JSON字符串 */
    private String query;

    /** 文档内容JSON字符串（insertOne/updateOne时使用） */
    private String document;
}
