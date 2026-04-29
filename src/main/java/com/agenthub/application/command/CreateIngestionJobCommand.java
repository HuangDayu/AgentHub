package com.agenthub.application.command;

import java.util.List;
import java.util.Objects;

/**
 * 创建入库任务命令。
 * <p>
 * 封装创建入库任务所需的知识库ID和文档负载列表。
 * </p>
 */
public record CreateIngestionJobCommand(
        String kbId,
        List<DocumentPayload> documents
) {
    public CreateIngestionJobCommand {
        Objects.requireNonNull(kbId, "kbCode must not be null");
        documents = documents == null ? List.of() : List.copyOf(documents);
    }

    /**
     * 文档负载记录，封装待上传文档的基本元数据。
     */
    public record DocumentPayload(
            String fileName,
            String contentType,
            long size
    ) {
    }
}
