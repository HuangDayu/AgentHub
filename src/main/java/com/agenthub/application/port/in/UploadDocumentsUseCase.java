package com.agenthub.application.port.in;

import com.agenthub.domain.model.IngestionJob;

/**
 * 上传文档到知识库的用例端口。
 */
public interface UploadDocumentsUseCase {

    /**
     * 上传文档到指定知识库，创建入库任务并触发流水线。
     *
     * @param kbId        知识库ID
     * @param fileName    文件名
     * @param contentType 内容类型
     * @param size        文件大小（字节）
     * @param storagePath MinIO存储路径
     * @return 创建的入库任务
     */
    IngestionJob uploadDocument(String kbId, String fileName, String contentType, long size, String storagePath);
}
