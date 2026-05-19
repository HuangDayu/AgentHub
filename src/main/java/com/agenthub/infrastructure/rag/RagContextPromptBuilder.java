package com.agenthub.infrastructure.rag;

import com.agenthub.domain.model.agent.ChatMessage;
import com.agenthub.domain.model.rag.RetrievalChunk;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RAG Prompt 组装器。
 * 将检索结果与用户问题组合，生成适合 LLM 回答的完整 Prompt。
 */
@Component
public class RagContextPromptBuilder {

    private static final String DEFAULT_RAG_TEMPLATE =
            "请基于以下背景信息回答问题。如果信息不足，请说明。\n\n" +
                    "## 背景信息\n" +
                    "{context}\n\n" +
                    "## 问题\n" +
                    "{question}";


    /**
     * 组装 RAG Prompt。
     *
     * @param userPrompt 用户问题
     * @param chunks     检索到的文档片段
     * @return 组装后的完整 Prompt
     */
    public List<ChatMessage> build(String sessionId, String template, String userPrompt, List<RetrievalChunk> chunks) {
        String tpl = resolveTemplate(template);
        String prompt = (chunks == null || chunks.isEmpty()) ? tpl : applyTemplate(tpl, buildContext(chunks), userPrompt);
        return List.of(ChatMessage.assistant(sessionId, prompt), ChatMessage.user(sessionId, userPrompt));
    }

    /**
     * 解析模板，空则使用默认。
     */
    private String resolveTemplate(String template) {
        return (template != null && !template.isBlank())
                ? template
                : DEFAULT_RAG_TEMPLATE;
    }

    /**
     * 构建上下文内容，按分数降序排列。
     */
    private String buildContext(List<RetrievalChunk> chunks) {
        return chunks.stream()
                .sorted(Comparator.comparingDouble(RetrievalChunk::getScore).reversed())
                .map(this::formatChunk)
                .collect(Collectors.joining("\n"));
    }

    /**
     * 格式化单个文档片段。
     */
    private String formatChunk(RetrievalChunk c) {
        String title = c.getDocumentTitle() != null ? c.getDocumentTitle() : c.getDocumentId();
        return "- [%s] (score: %.4f)\n  %s".formatted(title, c.getScore(), c.getContent());
    }

    /**
     * 应用模板替换占位符。
     */
    private String applyTemplate(String template, String context, String userPrompt) {
        return template
                .replace("{context}", context)
                .replace("{question}", userPrompt);
    }

    /**
     * 默认 RAG 模板。
     */
    public static String defaultTemplate() {
        return DEFAULT_RAG_TEMPLATE;
    }
}
