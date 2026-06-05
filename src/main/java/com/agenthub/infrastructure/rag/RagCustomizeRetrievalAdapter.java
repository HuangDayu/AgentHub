package com.agenthub.infrastructure.rag;

import com.agenthub.application.command.RagCommand;
import com.agenthub.application.command.RetrievalCommand;
import com.agenthub.application.dto.CitationOutput;
import com.agenthub.application.dto.RetrievalOutput;
import com.agenthub.application.dto.RetrievalResultOutput;
import com.agenthub.application.port.out.rag.*;
import com.agenthub.application.port.out.repositories.KnowledgeBaseRepository;
import com.agenthub.domain.exception.NotFoundException;
import com.agenthub.domain.model.rag.RetrievalChunk;
import com.agenthub.domain.model.rag.RetrievalResult;
import com.agenthub.domain.model.strategy.RetrievalStrategy;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.IntStream;

/**
 * Retrieve 用例
 */
@Component
@RequiredArgsConstructor
public class RagCustomizeRetrievalAdapter implements RagRetrievalPort {
    private static final Logger log = LoggerFactory.getLogger(RagCustomizeRetrievalAdapter.class);

    private final KnowledgeBaseRepository knowledgeBaseRepository;
    private final RagVectorSearchPort ragVectorSearchPort;
    private final RagTextSearchPort ragTextSearchPort;
    private final RagQueryRewritePort ragQueryRewritePort;
    private final RagRerankerPort ragRerankerPort;

    /**
     * 执行检索请求。
     *
     * @param query 检索查询
     * @return 检索结果列表
     */
    public List<RetrievalChunk> retrieve(RagCommand query) {
        List<RetrievalChunk> allChunks = new ArrayList<>();
        for (String kbId : query.getKbIds()) {
            addChunksFromKnowledgeBase(allChunks, kbId, query);
        }
        return sortAndLimit(allChunks, query.getStrategy().getTopK());
    }

    private void addChunksFromKnowledgeBase(List<RetrievalChunk> sink, String kbId, RagCommand query) {
        List<RetrievalChunk> retrievalChunks = retrieveFromKnowledgeBase(kbId, query);
        if (!retrievalChunks.isEmpty()) {
            sink.addAll(retrievalChunks);
        }
    }

    /**
     * 从单个知识库检索并添加结果。
     */
    private List<RetrievalChunk> retrieveFromKnowledgeBase(String kbId, RagCommand query) {
        try {
            RetrievalOutput result = retrieve(buildRetrievalCommand(kbId, query));
            return addChunksToResult(result, kbId);
        } catch (NotFoundException e) {
            log.warn("Knowledge base not found during retrieval: {}, skipping", kbId);
        } catch (Exception e) {
            log.error("Retrieval failed for knowledge base {}: {}", kbId, e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    private RetrievalCommand buildRetrievalCommand(String kbId, RagCommand query) {
        RetrievalStrategy strategy = query.getStrategy();
        return new RetrievalCommand(kbId, query.getPrompt(), strategy.getTopK(), strategy.getScoreThreshold(),
                strategy.isEnableQueryRewrite(), strategy.isEnableVectorSearch(), strategy.isEnableTextSearch(),
                strategy.isEnableRerank(), strategy.getRerankModel(), strategy.getVectorWeight(), strategy.getKeywordWeight());
    }

    /**
     * 添加检索结果到块列表。
     */
    private List<RetrievalChunk> addChunksToResult(RetrievalOutput result, String kbId) {
        List<RetrievalChunk> chunks = new ArrayList<>();
        for (RetrievalResultOutput item : result.getResults()) {
            chunks.add(new RetrievalChunk(
                    item.getContent(),
                    item.getDocumentId(),
                    item.getDocumentId(),
                    item.getChunkId(),
                    item.getScore(),
                    kbId
            ));
        }
        return chunks;
    }

    /**
     * 排序并限制结果数量。
     */
    private List<RetrievalChunk> sortAndLimit(List<RetrievalChunk> allChunks, int topK) {
        return allChunks.stream()
                .distinct()
                .sorted(Comparator.comparingDouble(RetrievalChunk::getScore).reversed())
                .limit(topK)
                .toList();
    }


    /**
     * 执行检索流水线：查询改写→向量/文本检索→合并去重→重排→过滤→截断→构建引用。
     *
     * @param retrievalCommand 检索命令，包含知识库ID、查询文本、Top-K和分数阈值
     * @return 检索结果，包含结果列表和引用列表
     */
    @Override
    public RetrievalOutput retrieve(RetrievalCommand retrievalCommand) {
        validateKnowledgeBase(retrievalCommand.getKbId());
        String rewrittenQuery = rewriteQuery(retrievalCommand);
        List<RetrievalResult> merged = searchAllSources(retrievalCommand, rewrittenQuery);
        List<RetrievalResult> reranked = rerankResults(retrievalCommand, rewrittenQuery, merged);
        List<RetrievalResult> filtered = filterByScore(reranked, retrievalCommand.getScoreThreshold());
        return buildRetrievalOutput(rewrittenQuery, limitResults(filtered, retrievalCommand.getTopK()));
    }

    private List<RetrievalResult> searchAllSources(RetrievalCommand cmd, String query) {
        return mergeResults(vectorSearch(cmd, query), textSearch(cmd, query));
    }

    private String rewriteQuery(RetrievalCommand cmd) {
        if (cmd.isEnableQueryRewrite()) {
            return ragQueryRewritePort.rewrite(cmd.getKbId(), cmd.getQuery());
        }
        return cmd.getQuery();
    }

    private List<RetrievalResult> vectorSearch(RetrievalCommand cmd, String query) {
        if (!cmd.isEnableVectorSearch()) return new ArrayList<>();
        return ragVectorSearchPort.search(cmd.getKbId(), query, cmd.getTopK());
    }

    private List<RetrievalResult> textSearch(RetrievalCommand cmd, String query) {
        if (!cmd.isEnableTextSearch()) return new ArrayList<>();
        return ragTextSearchPort.search(cmd.getKbId(), query, cmd.getTopK());
    }

    private List<RetrievalResult> rerankResults(RetrievalCommand cmd, String query, List<RetrievalResult> merged) {
        if (!cmd.isEnableRerank()) return new ArrayList<>();
        return ragRerankerPort.rerank(query, merged);
    }

    /**
     * 验证知识库存在。
     */
    private void validateKnowledgeBase(String kbId) {
        if (!knowledgeBaseRepository.existsById(kbId)) {
            throw new NotFoundException("knowledge base not found: " + kbId);
        }
    }

    /**
     * 合并向量和文本检索结果，按 chunkId 去重。
     */
    private List<RetrievalResult> mergeResults(
            List<RetrievalResult> vectorResults,
            List<RetrievalResult> textResults
    ) {
        Map<String, RetrievalResult> deduplicated = new LinkedHashMap<>();
        for (RetrievalResult result : vectorResults) {
            deduplicated.putIfAbsent(result.getChunkId(), result);
        }
        for (RetrievalResult result : textResults) {
            deduplicated.putIfAbsent(result.getChunkId(), result);
        }
        return new ArrayList<>(deduplicated.values());
    }

    /**
     * 按分数阈值过滤结果。
     */
    private List<RetrievalResult> filterByScore(List<RetrievalResult> results, double threshold) {
        return results.stream()
                .filter(r -> r.getScore() >= threshold)
                .toList();
    }

    /**
     * 限制结果数量。
     */
    private List<RetrievalResult> limitResults(List<RetrievalResult> results, int topK) {
        return results.stream().limit(topK).toList();
    }


    /**
     * 截取内容摘要。
     */
    private String excerpt(String content) {
        int maxLen = Math.min(content.length(), 200);
        return content.substring(0, maxLen);
    }

    /**
     * 转换为输出DTO结果。
     */
    private RetrievalOutput buildRetrievalOutput(String rewrittenQuery, List<RetrievalResult> results) {
        List<RetrievalResultOutput> outputResults = results.stream()
                .map(r -> new RetrievalResultOutput(r.getDocumentId(), r.getChunkId(), r.getContent(), r.getScore(), r.getMetadata()))
                .toList();
        List<CitationOutput> outputCitations = IntStream.range(0, results.size())
                .mapToObj(i -> {
                    RetrievalResult r = results.get(i);
                    return new CitationOutput(i + 1, r.getDocumentId(), r.getChunkId(), excerpt(r.getContent()));
                })
                .toList();
        return new RetrievalOutput(rewrittenQuery, outputResults, outputCitations);
    }


}
