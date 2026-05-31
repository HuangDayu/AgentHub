package com.agenthub.infrastructure.workflow.processor.impl;

import com.agenthub.application.command.RetrievalCommand;
import com.agenthub.application.dto.RetrievalOutput;
import com.agenthub.application.dto.RetrievalResultOutput;
import com.agenthub.application.port.out.rag.RagRetrievalPort;
import com.agenthub.domain.enums.workflow.DagNodeType;
import com.agenthub.domain.model.workflow.DagWorkflowContext;
import com.agenthub.domain.model.workflow.DagWorkflowNode;
import com.agenthub.infrastructure.workflow.processor.AbstractNodeProcessor;
import com.agenthub.infrastructure.workflow.variable.VariableResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 知识检索节点处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RetrievalNodeProcessor extends AbstractNodeProcessor {

    private final RagRetrievalPort ragRetrievalPort;
    private final VariableResolver variableResolver;

    @Override
    public String getSupportedType() {
        return DagNodeType.RETRIEVAL.name();
    }

    @Override
    protected Mono<Map<String, Object>> doProcess(DagWorkflowNode node, DagWorkflowContext context) {
        RetrievalCommand command = buildCommand(node, context);
        return executeRetrieval(command, node, context);
    }

    private RetrievalCommand buildCommand(DagWorkflowNode node, DagWorkflowContext context) {
        Map<String, Object> config = node.getConfig().getParameters();
        RetrievalCommand command = new RetrievalCommand();

        command.setKbId(getKnowledgeBaseId(config));
        command.setQuery(resolveQuery(config, context));
        command.setTopK(getTopK(config));
        command.setScoreThreshold(getScoreThreshold(config));
        setRetrievalType(command, config);

        return command;
    }

    private String getKnowledgeBaseId(Map<String, Object> config) {
        return (String) config.getOrDefault("knowledgeBaseId", "");
    }

    private String resolveQuery(Map<String, Object> config, DagWorkflowContext context) {
        String template = (String) config.getOrDefault("query", "");
        return variableResolver.resolveTemplateString(template, context);
    }

    private int getTopK(Map<String, Object> config) {
        Object topKValue = config.getOrDefault("topK", 5);
        if (topKValue instanceof Number) {
            return ((Number) topKValue).intValue();
        }
        return 5;
    }

    private double getScoreThreshold(Map<String, Object> config) {
        Object thresholdValue = config.getOrDefault("scoreThreshold", 0.5);
        if (thresholdValue instanceof Number) {
            return ((Number) thresholdValue).doubleValue();
        }
        return 0.5;
    }

    private void setRetrievalType(RetrievalCommand command, Map<String, Object> config) {
        String type = (String) config.getOrDefault("retrievalType", "similarity");

        if ("hybrid".equals(type)) {
            command.setEnableVectorSearch(true);
            command.setEnableTextSearch(true);
            command.setVectorWeight(0.7);
            command.setKeywordWeight(0.3);
        } else {
            command.setEnableVectorSearch(true);
            command.setEnableTextSearch(false);
        }
    }

    private Mono<Map<String, Object>> executeRetrieval(
            RetrievalCommand command, DagWorkflowNode node, DagWorkflowContext context) {
        log.info("Retrieving from KB: {}, query: {}", command.getKbId(), command.getQuery());
        return Mono.fromCallable(() -> doRetrieve(command, node, context));
    }

    private Map<String, Object> doRetrieve(
            RetrievalCommand command, DagWorkflowNode node, DagWorkflowContext context) {
        try {
            return executeRetrievalSafely(command, node, context);
        } catch (Exception e) {
            return handleRetrievalFailure(node, context, e);
        }
    }

    /**
     * 安全执行检索。
     */
    private Map<String, Object> executeRetrievalSafely(
            RetrievalCommand command, DagWorkflowNode node, DagWorkflowContext context) {
        RetrievalOutput output = ragRetrievalPort.retrieve(command);
        return processResult(output, node, context);
    }

    /**
     * 处理检索失败，返回空结果。
     */
    private Map<String, Object> handleRetrievalFailure(
            DagWorkflowNode node, DagWorkflowContext context, Exception e) {
        log.warn("Retrieval failed, returning empty result: {}", e.getMessage());
        Map<String, Object> result = createEmptyResult(e);
        saveResultToContext(result, node, context);
        return result;
    }

    /**
     * 创建空结果。
     */
    private Map<String, Object> createEmptyResult(Exception e) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("documentCount", 0);
        result.put("documents", new ArrayList<>());
        result.put("error", e.getMessage());
        return result;
    }

    /**
     * 保存结果到上下文。
     */
    private void saveResultToContext(Map<String, Object> result, DagWorkflowNode node, DagWorkflowContext context) {
        Map<String, Object> config = node.getConfig().getParameters();
        saveToContext(result, config, context);
    }

    private Map<String, Object> processResult(
            RetrievalOutput output, DagWorkflowNode node, DagWorkflowContext context) {
        Map<String, Object> config = node.getConfig().getParameters();
        List<RetrievalResultOutput> results = output.getResults();

        Map<String, Object> result = createBaseResult(results);
        addDocumentsByMode(result, results, config);
        saveToContext(result, config, context);

        log.info("Retrieved {} documents", results.size());
        return result;
    }

    private Map<String, Object> createBaseResult(List<RetrievalResultOutput> results) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("documentCount", results.size());
        return result;
    }

    private void addDocumentsByMode(
            Map<String, Object> result, List<RetrievalResultOutput> results, Map<String, Object> config) {
        String mode = (String) config.getOrDefault("processMode", "list");

        switch (mode) {
            case "concat" -> addConcatenatedContent(result, results, config);
            case "structured" -> addStructuredDocuments(result, results);
            default -> addDocumentList(result, results, config);
        }
    }

    private void addDocumentList(
            Map<String, Object> result, List<RetrievalResultOutput> results, Map<String, Object> config) {
        boolean includeMetadata = (boolean) config.getOrDefault("includeMetadata", false);
        boolean includeScores = (boolean) config.getOrDefault("includeScores", true);

        List<Map<String, Object>> docs = new ArrayList<>();
        for (RetrievalResultOutput doc : results) {
            docs.add(buildDocumentMap(doc, includeMetadata, includeScores));
        }
        result.put("documents", docs);
    }

    private Map<String, Object> buildDocumentMap(
            RetrievalResultOutput doc, boolean includeMetadata, boolean includeScores) {
        Map<String, Object> docMap = new HashMap<>();
        docMap.put("content", doc.getContent());

        if (includeMetadata && doc.getMetadata() != null) {
            docMap.put("metadata", doc.getMetadata());
        }
        if (includeScores) {
            docMap.put("score", doc.getScore());
        }

        return docMap;
    }

    private void addConcatenatedContent(
            Map<String, Object> result, List<RetrievalResultOutput> results, Map<String, Object> config) {
        String separator = (String) config.getOrDefault("separator", "\n\n");
        String content = String.join(separator,
                results.stream().map(RetrievalResultOutput::getContent).toList());
        result.put("content", content);
    }

    private void addStructuredDocuments(
            Map<String, Object> result, List<RetrievalResultOutput> results) {
        List<Map<String, Object>> docs = new ArrayList<>();
        for (RetrievalResultOutput doc : results) {
            docs.add(buildStructuredDocumentMap(doc));
        }
        result.put("documents", docs);
    }

    private Map<String, Object> buildStructuredDocumentMap(RetrievalResultOutput doc) {
        Map<String, Object> docMap = new HashMap<>();
        docMap.put("content", doc.getContent());
        docMap.put("metadata", doc.getMetadata());
        docMap.put("score", doc.getScore());
        return docMap;
    }

    private void saveToContext(
            Map<String, Object> result, Map<String, Object> config, DagWorkflowContext context) {
        String varName = (String) config.getOrDefault("outputVariable", "retrievedDocs");
        context.setVariable(varName, result);
    }
}
