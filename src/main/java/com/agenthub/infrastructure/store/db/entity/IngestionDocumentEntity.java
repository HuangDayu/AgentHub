package com.agenthub.infrastructure.store.db.entity;
import lombok.Data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 入库文档数据库实体。
 * <p>
 * 映射到 app.ingestion_document 表，存储文档的元数据和处理状态。
 * </p>
 */
@Data
@TableName("app.ingestion_document")
public class IngestionDocumentEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    private String kbId;
    private String jobId;
    private String fileName;
    private String contentType;
    private Long size;
    private String storagePath;
    private String status;
}
