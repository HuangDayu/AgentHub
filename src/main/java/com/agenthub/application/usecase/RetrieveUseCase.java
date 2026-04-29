package com.agenthub.application.usecase;

import com.agenthub.application.port.out.rag.*;
import com.agenthub.common.exception.NotFoundException;
import com.agenthub.application.dto.CitationOutput;
import com.agenthub.application.dto.RetrievalOutput;
import com.agenthub.application.dto.RetrievalResultOutput;
import com.agenthub.application.port.out.repositories.KnowledgeBaseRepository;
import com.agenthub.domain.model.RetrievalResult;
import com.agenthub.domain.model.RetrievalChunk;
import com.agenthub.domain.model.RetrievalQuery;
import com.agenthub.domain.model.RetrievalStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.IntStream;

/**
 * Retrieve 用例
 */
@Component
public class RetrieveUseCase implements RetrievalPort {
    private static final Logger log = LoggerFactory.getLogger(RetrieveUseCase.class);

    private final KnowledgeBaseRepository knowledgeBaseRepository;
    private final VectorSearchPort vectorSearchPort;
    private final TextSearchPort textSearchPort;
    private final QueryRewritePort queryRewritePort;
    private final RerankerPort rerankerPort;

    /**
     * 构造函数，注入知识库仓库、向量检索、文本检索、查询改写和重排器端口。
     */
    public RetrieveUseCase(
            KnowledgeBaseRepository knowledgeBaseRepository,
            VectorSearchPort vectorSearchPort,
            TextSearchPort textSearchPort,
            QueryRewritePort queryRewritePort,
            RerankerPort rerankerPort
    ) {
        this.knowledgeBaseRepository = knowledgeBaseRepository;
        this.vectorSearchPort = vectorSearchPort;
        this.textSearchPort = textSearchPort;
        this.queryRewritePort = queryRewritePort;
        this.rerankerPort = rerankerPort;
    }

    /**
     * 执行检索请求。
     *
     * @param query 检索查询
     * @return 检索结果列表
     */
    public List<RetrievalChunk> retrieve(RetrievalQuery query) {
        List<RetrievalChunk> allChunks = new ArrayList<>();
        for (String kbId : query.kbIds()) {
            retrieveFromKnowledgeBase(kbId, query, allChunks);
        }
        return sortAndLimit(allChunks, query.strategy().getTopK());
    }

    /**
     * 从单个知识库检索并添加结果。
     */
    private void retrieveFromKnowledgeBase(String kbId, RetrievalQuery query, List<RetrievalChunk> allChunks) {
        try {
            RetrievalStrategy strategy = query.strategy();
            Command command = new Command(kbId, query.query(), strategy.getTopK(), strategy.getScoreThreshold(),
                    strategy.isEnableQueryRewrite(), strategy.isEnableVectorSearch(), strategy.isEnableTextSearch(),
                    strategy.isEnableRerank(), strategy.getRerankModel(), strategy.getVectorWeight(), strategy.getKeywordWeight());
            RetrievalOutput result = retrieve(command);
            addChunksToResult(allChunks, result, kbId);
        } catch (NotFoundException e) {
            log.warn("Knowledge base not found during retrieval: {}, skipping", kbId);
        } catch (Exception e) {
            log.error("Retrieval failed for knowledge base {}: {}", kbId, e.getMessage(), e);
        }
    }

    /**
     * 添加检索结果到块列表。
     */
    private void addChunksToResult(List<RetrievalChunk> allChunks, RetrievalOutput result, String kbId) {
        for (RetrievalResultOutput item : result.results()) {
            allChunks.add(new RetrievalChunk(
                    item.content(),
                    item.documentId(),
                    item.documentId(),
                    item.chunkId(),
                    item.score(),
                    kbId
            ));
        }
    }

    /**
     * 排序并限制结果数量。
     */
    private List<RetrievalChunk> sortAndLimit(List<RetrievalChunk> allChunks, int topK) {
        return allChunks.stream()
                .distinct()
                .sorted(Comparator.comparingDouble(RetrievalChunk::score).reversed())
                .limit(topK)
                .toList();
    }


    /**
     * 执行检索流水线：查询改写→向量/文本检索→合并去重→重排→过滤→截断→构建引用。
     *
     * @param command 检索命令，包含知识库ID、查询文本、Top-K和分数阈值
     * @return 检索结果，包含结果列表和引用列表
     */
    public RetrievalOutput retrieve(Command command) {
        validateKnowledgeBase(command.kbId());
        String rewrittenQuery = command.enableQueryRewrite() ? queryRewritePort.rewrite(command.kbId(), command.query()) : command.query();
        log.debug("Query rewritten [{}]: '{}' -> '{}'", command.enableQueryRewrite(), command.query(), rewrittenQuery);
        List<RetrievalResult> vectorResults = command.enableVectorSearch() ? vectorSearchPort.search(command.kbId(), rewrittenQuery, command.topK()) : new ArrayList<>();
        log.debug("Vector search [{}] returned {} results", command.enableVectorSearch(), vectorResults.size());
        List<RetrievalResult> textResults = command.enableTextSearch() ? textSearchPort.search(command.kbId(), rewrittenQuery, command.topK()) : new ArrayList<>();
        log.debug("Text search [{}] returned {} results", command.enableTextSearch(), textResults.size());
        List<RetrievalResult> merged = mergeResults(vectorResults, textResults);
        log.debug("After merge: {} results", merged.size());
        List<RetrievalResult> reranked = command.enableRerank() ? rerankerPort.rerank(rewrittenQuery, merged) : new ArrayList<>();
        log.debug("After rerank [{}]: {} results", command.enableRerank(), reranked.size());
        List<RetrievalResult> filtered = filterByScore(reranked, command.scoreThreshold());
        log.debug("After filter (threshold={}): {} results", command.scoreThreshold(), filtered.size());
        List<RetrievalResult> limited = limitResults(filtered, command.topK());
        log.debug("After limited (topK={}): {} results", command.topK(), filtered.size());
        return buildRetrievalOutput(rewrittenQuery, limited);
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
            deduplicated.putIfAbsent(result.chunkId(), result);
        }
        for (RetrievalResult result : textResults) {
            deduplicated.putIfAbsent(result.chunkId(), result);
        }
        return new ArrayList<>(deduplicated.values());
    }

    /**
     * 按分数阈值过滤结果。
     */
    private List<RetrievalResult> filterByScore(List<RetrievalResult> results, double threshold) {
        return results.stream()
                .filter(r -> r.score() >= threshold)
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
                .map(r -> new RetrievalResultOutput(r.documentId(), r.chunkId(), r.content(), r.score()))
                .toList();
        List<CitationOutput> outputCitations = IntStream.range(0, results.size())
                .mapToObj(i -> {
                    RetrievalResult r = results.get(i);
                    return new CitationOutput(i + 1, r.documentId(), r.chunkId(), excerpt(r.content()));
                })
                .toList();
        return new RetrievalOutput(rewrittenQuery, outputResults, outputCitations);
    }


    /**
     * 检索命令。
     */
    public record Command(
            String kbId,
            String query,
            int topK,
            double scoreThreshold,
            boolean enableQueryRewrite,
            boolean enableRerank,
            boolean enableTextSearch,
            boolean enableVectorSearch,
            String rerankModel,
            double vectorWeight,
            double keywordWeight
    ) {
    }


}
