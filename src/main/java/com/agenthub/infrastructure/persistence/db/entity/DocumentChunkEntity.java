package com.agenthub.infrastructure.persistence.db.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 文档分块数据库实体。
 * <p>
 * 映射到 app.document_chunk 表，存储文档分块的内容和向量嵌入。
 * </p>
 */
@TableName("app.document_chunk")
public class DocumentChunkEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    private String chunkId;
    private String documentId;
    private String kbId;
    private Integer chunkIndex;
    private Integer tokenCount;

    public String getChunkId() {
        return chunkId;
    }

    public void setChunkId(String chunkId) {
        this.chunkId = chunkId;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getKbId() {
        return kbId;
    }

    public void setKbId(String kbId) {
        this.kbId = kbId;
    }

    public Integer getChunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(Integer chunkIndex) {
        this.chunkIndex = chunkIndex;
    }

    public Integer getTokenCount() {
        return tokenCount;
    }

    public void setTokenCount(Integer tokenCount) {
        this.tokenCount = tokenCount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
