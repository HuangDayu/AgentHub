package com.agenthub.infrastructure.rag;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.HighlightField;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.agenthub.application.port.out.rag.RagTextSearchPort;
import com.agenthub.domain.model.rag.RetrievalResult;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Elasticsearch 全文检索适配器。
 */
@Component
public class RagElasticsearchTextSearchAdapter implements RagTextSearchPort {
    private static final Logger log = LoggerFactory.getLogger(RagElasticsearchTextSearchAdapter.class);

    private static final String INDEX_NAME = "knowledge_chunks";
    private static final String TENANT_ID_FIELD = "tenant_id";
    private static final String KB_ID_FIELD = "kb_id";
    private static final String CONTENT_FIELD = "content";
    private static final String DOCUMENT_ID_FIELD = "document_id";
    private static final int FUZZY_MAX_EXPANSIONS = 50;

    private final String host;
    private final int port;
    private final boolean enabled;
    private volatile ElasticsearchClient elasticsearchClient;
    private volatile boolean initialized = false;

    /**
     * 构造函数。
     */
    public RagElasticsearchTextSearchAdapter(
            @Value("${agenthub.text-search.enabled:false}") boolean enabled,
            @Value("${agenthub.text-search.host:localhost}") String host,
            @Value("${agenthub.text-search.port:9200}") int port
    ) {
        this.enabled = enabled;
        this.host = host;
        this.port = port;
        logInitialization();
    }

    /**
     * 记录初始化日志。
     */
    private void logInitialization() {
        if (enabled) {
            log.info("ElasticsearchTextSearchAdapter configured, target={}:{}", host, port);
        } else {
            log.info("ElasticsearchTextSearchAdapter disabled");
        }
    }

    /**
     * 执行全文搜索。
     */
    @Override
    public List<RetrievalResult> search(String kbId, String queryText, int topK) {
        if (!enabled || isInvalidInput(kbId, queryText)) {
            return Collections.emptyList();
        }
        return doSearch(kbId, queryText, topK);
    }

    /**
     * 检查输入是否无效。
     */
    private boolean isInvalidInput(String kbId, String queryText) {
        return kbId == null || kbId.isBlank() || queryText == null || queryText.isBlank();
    }

    /**
     * 执行搜索。
     */
    private List<RetrievalResult> doSearch(String kbId, String queryText, int topK) {
        ElasticsearchClient client = getClient();
        if (client == null) {
            log.warn("Elasticsearch client not available, returning empty results for kbCode={}", kbId);
            return Collections.emptyList();
        }
        return executeSearch(client, kbId, queryText, topK);
    }

    /**
     * 获取或初始化客户端。
     */
    private ElasticsearchClient getClient() {
        if (initialized) {
            return elasticsearchClient;
        }
        synchronized (this) {
            if (initialized) {
                return elasticsearchClient;
            }
            return initializeClient();
        }
    }

    /**
     * 初始化客户端。
     */
    private ElasticsearchClient initializeClient() {
        try {
            RestClient restClient = RestClient.builder(new HttpHost(host, port)).build();
            RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
            this.elasticsearchClient = new ElasticsearchClient(transport);
            this.initialized = true;
            log.info("Elasticsearch client initialized successfully, target={}:{}", host, port);
            return this.elasticsearchClient;
        } catch (Exception e) {
            log.error("Failed to initialize Elasticsearch client, target={}:{}", host, port, e);
            this.initialized = true;
            return null;
        }
    }

    /**
     * 执行搜索请求。
     */
    private List<RetrievalResult> executeSearch(ElasticsearchClient client, String kbId, String queryText, int topK) {
        try {
            SearchRequest searchRequest = buildSearchRequest(kbId, queryText, topK);
            SearchResponse<Map> response = client.search(searchRequest, Map.class);
            return toRetrievalResults(response);
        } catch (IOException e) {
            log.error("Text search failed for kbCode={}", kbId, e);
            return Collections.emptyList();
        } catch (ElasticsearchException e) {
            log.error("Elasticsearch exception for kbCode={}", kbId, e);
            return Collections.emptyList();
        }
    }

    /**
     * 构建搜索请求。
     */
    private SearchRequest buildSearchRequest(String kbId, String queryText, int topK) {
        return SearchRequest.of(s -> s
                .index(INDEX_NAME)
                .size(topK)
                .query(buildQuery(kbId, queryText))
                .highlight(h -> h.fields(CONTENT_FIELD, HighlightField.of(hf -> hf
                        .preTags("<em>").postTags("</em>").fragmentSize(200).numberOfFragments(3))))
                .source(src -> src.filter(f -> f.includes(DOCUMENT_ID_FIELD, CONTENT_FIELD)))
        );
    }

    /**
     * 构建查询条件。
     */
    private Query buildQuery(String kbId, String queryText) {
        return Query.of(q -> q.bool(b -> b
                .must(m -> m.match(mt -> mt
                        .field(CONTENT_FIELD).query(queryText).fuzziness("AUTO")
                        .maxExpansions(FUZZY_MAX_EXPANSIONS).prefixLength(1).operator(Operator.Or)))
                .filter(Query.of(f -> f.term(t -> t.field(KB_ID_FIELD).value(kbId))))
        ));
    }

    /**
     * 转换为检索结果列表。
     */
    private List<RetrievalResult> toRetrievalResults(SearchResponse<Map> response) {
        return response.hits().hits().stream().map(this::toRetrievalResult).toList();
    }

    private String resolveDisplayContent(Hit<Map> hit, String content) {
        String highlighted = extractHighlightedContent(hit);
        return highlighted != null ? highlighted : content != null ? content : "";
    }

    private static String nvl(String s) {
        return s != null ? s : "";
    }

    /**
     * 转换为单个检索结果。
     */
    private RetrievalResult toRetrievalResult(Hit<Map> hit) {
        Map<String, Object> source = hit.source() != null ? hit.source() : Collections.emptyMap();
        String docId = nvl(extractString(source, DOCUMENT_ID_FIELD));
        String content = extractString(source, CONTENT_FIELD);
        String display = resolveDisplayContent(hit, content);
        double score = hit.score() != null ? normalizeScore(hit.score()) : 0.0;
        return new RetrievalResult(docId, null, nvl(hit.id()), display, score, source);
    }

    /**
     * 提取高亮内容。
     */
    private String extractHighlightedContent(Hit<Map> hit) {
        if (hit.highlight() == null || !hit.highlight().containsKey(CONTENT_FIELD)) {
            return null;
        }
        List<String> fragments = hit.highlight().get(CONTENT_FIELD);
        if (fragments == null || fragments.isEmpty()) {
            return null;
        }
        return String.join(" ... ", fragments);
    }

    /**
     * 归一化分数。
     */
    private double normalizeScore(double rawScore) {
        double normalized = 1.0 / (1.0 + Math.exp(-rawScore / 2.0));
        return Math.max(0.0, Math.min(1.0, normalized));
    }

    /**
     * 提取字符串值。
     */
    private String extractString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value instanceof String str ? str : null;
    }
}
