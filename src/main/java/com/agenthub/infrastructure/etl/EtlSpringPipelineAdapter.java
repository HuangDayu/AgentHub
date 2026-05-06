package com.agenthub.infrastructure.etl;

import com.agenthub.application.command.EtlCommand;
import com.agenthub.application.port.out.etl.ExtractTransformLoadPort;
import com.agenthub.domain.model.DocumentChunk;
import com.agenthub.domain.model.DocumentContent;
import com.agenthub.infrastructure.factory.SpringShareObjectFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.model.transformer.KeywordMetadataEnricher;
import org.springframework.ai.model.transformer.SummaryMetadataEnricher;
import org.springframework.ai.reader.JsonReader;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author huangdayu
 * @doc https://docs.spring.io/spring-ai/reference/2.0/api/etl-pipeline.html
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "agenthub.etl.impl-type", havingValue = "spring", matchIfMissing = true)
public class EtlSpringPipelineAdapter implements ExtractTransformLoadPort {
    private final SpringShareObjectFactory springShareObjectFactory;

    @Override
    public List<DocumentChunk> etl(EtlCommand etlCommand) {
        ChatModel chatModel = springShareObjectFactory.getChatModelByKbId(etlCommand.getKbId());
        VectorStore vectorStore = springShareObjectFactory.getVectorStoreByKbId(etlCommand.getKbId());
        List<Document> documents = loadDocument(etlCommand);
        documents = splitterDocuments(documents);
        documents = enrichDocuments(chatModel, documents);
        documents = summaryDocuments(chatModel, documents);
        vectorStore.add(documents);
        return documents.stream()
                .map(d -> DocumentChunk.reconstruct(d.getId(), etlCommand.getDocumentId(), etlCommand.getKbId(), d.hashCode(), "null", 0, null))
                .collect(Collectors.toList());
    }

    @Override
    public boolean delete(List<DocumentChunk> documentChunks) {
        String kbId = documentChunks.getFirst().getKbId();
        VectorStore vectorStore = springShareObjectFactory.getVectorStoreByKbId(kbId);
        List<String> collect = documentChunks.stream().map(DocumentChunk::getChunkId).collect(Collectors.toList());
        vectorStore.delete(collect);
        return true;
    }

    /**
     * 基于令牌计数将文本分割成块
     *
     * @param documents
     * @return
     */
    public List<Document> splitterDocuments(List<Document> documents) {
        TokenTextSplitter splitter = TokenTextSplitter.builder()
                .withChunkSize(1000)
                .withMinChunkSizeChars(400)
                .withMinChunkLengthToEmbed(10)
                .withMaxNumChunks(5000)
                .withPunctuationMarks(List.of('.', '?', '!', '\n', ';', ':', '。'))
                .withKeepSeparator(true)
                .build();
        return splitter.apply(documents);
    }

    /**
     * 利用生成式AI模型从文档内容中提取关键词并添加为元数据
     *
     * @param chatModel
     * @param documents
     * @return
     */
    List<Document> enrichDocuments(ChatModel chatModel, List<Document> documents) {
        KeywordMetadataEnricher enricher = KeywordMetadataEnricher.builder(chatModel)
                .keywordCount(5)
                .build();
        return enricher.apply(documents);
    }

    /**
     * 使用生成式人工智能模型为文档创建摘要并将其添加为元数据
     *
     * @param chatModel
     * @param documents
     * @return
     */
    private List<Document> summaryDocuments(ChatModel chatModel, List<Document> documents) {
        SummaryMetadataEnricher enricher = new SummaryMetadataEnricher(chatModel,
                List.of(SummaryMetadataEnricher.SummaryType.PREVIOUS,
                        SummaryMetadataEnricher.SummaryType.CURRENT,
                        SummaryMetadataEnricher.SummaryType.NEXT));
        return enricher.apply(documents);
    }


    private List<Document> loadDocument(EtlCommand etlCommand) {
        DocumentContent.DocumentFormat format = DocumentContent.detectFormat(etlCommand.getContentType(), etlCommand.getFileName());
        InputStreamResource inputStreamResource = new InputStreamResource(etlCommand.getInputStream());
        DocumentReader documentReader = switch (format) {
            case JSON -> new JsonReader(inputStreamResource);
            case TEXT -> new TextReader(inputStreamResource);
            case MARKDOWN -> loadMarkdown(inputStreamResource);
            case PDF, WORD, EXCEL, UNKNOWN, HTML -> new TikaDocumentReader(inputStreamResource);
        };
        return documentReader.read();
    }

    private DocumentReader loadMarkdown(InputStreamResource inputStreamResource) {
        MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                .withHorizontalRuleCreateDocument(true)
                .withIncludeCodeBlock(false)
                .withIncludeBlockquote(false)
                .build();
        return new MarkdownDocumentReader(inputStreamResource, config);
    }

}
