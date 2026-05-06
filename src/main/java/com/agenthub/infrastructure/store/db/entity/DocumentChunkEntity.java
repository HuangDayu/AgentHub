package com.agenthub.infrastructure.store.db.entity;
import lombok.Data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 文档分块数据库实体。
 * <p>
 * 映射到 app.document_chunk 表，存储文档分块的内容和向量嵌入。
 * </p>
 */
@Data
@TableName("app.document_chunk")
public class DocumentChunkEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    private String chunkId;
    private String documentId;
    private String kbId;
    private Integer chunkIndex;
    private Integer tokenCount;

}
