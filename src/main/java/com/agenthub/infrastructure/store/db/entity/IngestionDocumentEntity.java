package com.agenthub.infrastructure.store.db.entity;
import lombok.Data;

import com.agenthub.domain.annotation.AgentDataField;
import com.agenthub.domain.annotation.AgentDataModel;
import com.agenthub.infrastructure.store.db.mapper.IngestionDocumentMybatisMapper;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 入库文档数据库实体。
 * <p>
 * 映射到 ingestion_document 表，存储文档的元数据和处理状态。
 * </p>
 */
@Data
@TableName("ingestion_document")
@AgentDataModel(
    name = "入库文档",
    description = "知识库入库文档，记录文档元数据和处理状态",
    domain = "知识管理",
    mapper = IngestionDocumentMybatisMapper.class
)
public class IngestionDocumentEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    @AgentDataField(description = "知识库ID", filterable = true)
    private String kbId;
    @AgentDataField(description = "入库任务ID")
    private String jobId;
    @AgentDataField(description = "文件名")
    private String fileName;
    @AgentDataField(description = "内容类型")
    private String contentType;
    @AgentDataField(description = "文件大小")
    private Long size;
    @AgentDataField(description = "存储路径")
    private String storagePath;
    @AgentDataField(description = "处理状态", filterable = true)
    private String status;
}
