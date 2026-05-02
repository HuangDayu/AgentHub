package com.agenthub.infrastructure.rag;

import com.agenthub.application.port.out.rag.RagRerankerPort;
import com.agenthub.domain.model.RetrievalResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 基于 Cross-Encoder 的重排器适配器。
 * <p>
 * 调用外部 Cross-Encoder 服务对检索结果进行重排。
 * </p>
 */
@Component
@ConditionalOnProperty(name = "agenthub.rag.reranker.type", havingValue = "cross-encoder")
public class CrossEncoderRagRerankerAdapter implements RagRerankerPort {
    private static final Logger log = LoggerFactory.getLogger(CrossEncoderRagRerankerAdapter.class);

    @Value("${reranker.cross-encoder.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate;

    public CrossEncoderRagRerankerAdapter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 对检索结果进行重排。
     *
     * @param queryText 查询文本
     * @param results   原始检索结果
     * @return 重排后的结果列表
     */
    @Override
    public List<RetrievalResult> rerank(String queryText, List<RetrievalResult> results) {
        if (results.isEmpty()) {
            return results;
        }
        List<ScoredCandidate> scoredCandidates = callCrossEncoder(queryText, results);
        return scoredCandidates.stream()
                .map(c -> mapToResult(c, results))
                .toList();
    }

    /**
     * 调用 Cross-Encoder 服务。
     */
    private List<ScoredCandidate> callCrossEncoder(String queryText, List<RetrievalResult> results) {
        RerankInput input = createRerankInput(queryText, results);
        RerankOutput output = callRerankService(input);
        if (output == null || output.scores() == null) {
            return Collections.emptyList();
        }
        return parseScores(output);
    }

    /**
     * 创建重排请求。
     */
    private RerankInput createRerankInput(String queryText, List<RetrievalResult> results) {
        return new RerankInput(queryText, results.stream().map(RetrievalResult::content).toList());
    }

    /**
     * 调用重排服务。
     */
    private RerankOutput callRerankService(RerankInput input) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RerankInput> entity = new HttpEntity<>(input, headers);
        return restTemplate.postForObject(baseUrl + "/v1/rerank", entity, RerankOutput.class);
    }

    /**
     * 解析评分响应。
     */
    private List<ScoredCandidate> parseScores(RerankOutput output) {
        return output.scores().entrySet().stream()
                .map(e -> new ScoredCandidate(Integer.parseInt(e.getKey()), e.getValue()))
                .toList();
    }

    /**
     * 将评分后的候选映射为领域RetrievalResult。
     *
     * @param scored    评分候选
     * @param originals 原始检索结果列表
     * @return 更新分数后的检索结果
     */
    private RetrievalResult mapToResult(ScoredCandidate scored, List<RetrievalResult> originals) {
        if (scored.index() < 0 || scored.index() >= originals.size()) {
            return originals.get(0);
        }
        RetrievalResult original = originals.get(scored.index());
        return new RetrievalResult(
                original.documentId(),
                original.documentTitle(),
                original.chunkId(),
                original.content(),
                Math.min(scored.score(), 1.0)
        );
    }

    /**
     * 重排输入数据。
     */
    private record RerankInput(String query, List<String> candidates) {
    }

    /**
     * 重排输出数据。
     */
    private record RerankOutput(Map<String, Double> scores) {
    }

    /**
     * 评分候选记录，包含索引和分数。
     */
    private record ScoredCandidate(int index, double score) {
    }
}
