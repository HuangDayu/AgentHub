package com.agenthub.infrastructure.etl;

import com.agenthub.application.command.EtlCommand;
import com.agenthub.application.port.out.etl.*;
import com.agenthub.domain.exception.IngestionPipelineException;
import com.agenthub.domain.model.etl.DocumentChunk;
import com.agenthub.domain.model.etl.DocumentContent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

/**
 * @author huangdayu
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "agenthub.etl.impl-type", havingValue = "customize")
public class EtlCustomizePipelineAdapter implements ExtractTransformLoadPort {

    private static final int DEFAULT_CHUNK_SIZE = 512;
    private static final int DEFAULT_OVERLAP_SIZE = 50;
    private final EtlDocumentParserPort documentParser;
    private final EtlDocumentCleanerPort contentCleaner;
    private final EtlDocumentChunkerPort documentChunker;
    private final EtlDocumentChunkStorePort etlDocumentChunkStorePort;

    @Override
    public List<DocumentChunk> etl(EtlCommand etlCommand) {
        List<DocumentChunk> documentChunks = processDocument(etlCommand);
        etlDocumentChunkStorePort.saveAll(etlCommand.getKbId(), documentChunks);
        return List.of();
    }

    @Override
    public boolean delete(List<DocumentChunk> documentChunks) {
        etlDocumentChunkStorePort.deleteAll(documentChunks.getFirst().getKbId(), documentChunks.stream().map(DocumentChunk::getChunkId).toList());
        return true;
    }

    /**
     * 处理单个文档：解析内容、清洗、分块。
     *
     * @return 该文档的分块列表
     */
    private List<DocumentChunk> processDocument(EtlCommand etlCommand) {
        DocumentContent parsed = parseDocument(etlCommand);
        DocumentContent cleaned = contentCleaner.clean(parsed);
        return documentChunker.chunk(etlCommand.getDocumentId(), etlCommand.getKbId(), cleaned.getCleanedContent(), DEFAULT_CHUNK_SIZE, DEFAULT_OVERLAP_SIZE
        );
    }

    /**
     * 从文档存储中读取并解析文档内容。
     *
     * @return 解析后的文档内容
     */
    private DocumentContent parseDocument(EtlCommand etlCommand) {
        try (InputStream content = etlCommand.getInputStream()) {
            return documentParser.parse(etlCommand.getDocumentId(), content, etlCommand.getContentType(), etlCommand.getFileName());
        } catch (Exception e) {
            throw new IngestionPipelineException("Failed to parse document: " + etlCommand.getDocumentId(), e);
        }
    }
}
