package com.agenthub.infrastructure.rag;

import com.agenthub.application.port.out.rag.RagQueryRewritePort;
import com.agenthub.infrastructure.pool.SpringAiObjectPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

import static com.agenthub.infrastructure.rag.RagPromptTemplate.REWRITE_PROMPT_TEMPLATE;

/**
 * 基于 LLM 的查询改写适配器。
 * <p>
 * 使用 Spring AI ChatClient 调用大语言模型，将用户原始查询改写为更精确的搜索关键词。
 * 支持中文和英文多语言场景。当 LLM 调用失败时，降级返回原始查询。
 * </p>
 *
 * <p>示例场景：
 * <ul>
 *   <li>用户输入 "Java 怎么实现多线程" → 改写为 "Java 多线程实现 synchronized Thread"</li>
 *   <li>用户输入 "how to deploy spring boot to k8s" → 改写为 "Spring Boot Kubernetes deployment guide"</li>
 * </ul>
 * </p>
 *
 * @author agenthub
 * @since 1.0.0
 */
@Component
public class RagSpringQueryRewriteAdapter implements RagQueryRewritePort {

    private static final Logger log = LoggerFactory.getLogger(RagSpringQueryRewriteAdapter.class);


    private final SpringAiObjectPoolManager springAiObjectPoolManager;

    public RagSpringQueryRewriteAdapter(SpringAiObjectPoolManager springAiObjectPoolManager) {
        this.springAiObjectPoolManager = springAiObjectPoolManager;
    }

    /**
     * 使用 LLM 改写查询文本。
     *
     * @param queryText 原始查询文本
     * @return 改写后的查询文本；若改写失败则返回原始查询
     */
    @Override
    public String rewrite(String kbId, String queryText) {
        if (queryText == null || queryText.isBlank()) {
            return queryText;
        }
        try {
            ChatModel chatModel = springAiObjectPoolManager.getChatModelByKbId(kbId);
            return callLlmForRewrite(chatModel, queryText);
        } catch (Exception e) {
            log.error("LLM prompt rewrite failed, falling back to original prompt: {}", e.getMessage(), e);
            return queryText;
        }
    }

    /**
     * 调用LLM执行改写。
     */
    private String callLlmForRewrite(ChatModel chatModel, String queryText) {
        String promptText = REWRITE_PROMPT_TEMPLATE.formatted(queryText.strip());
        Prompt prompt = new Prompt(promptText);
        log.debug("Sending prompt rewrite request to LLM, original prompt: {}", queryText);
        String rewritten = chatModel.call(prompt).getResult().getOutput().getText();
        return validateRewrittenResult(rewritten, queryText);
    }

    /**
     * 验证改写结果。
     */
    private String validateRewrittenResult(String rewritten, String queryText) {
        if (rewritten == null || rewritten.isBlank()) {
            log.warn("LLM returned empty rewrite result, falling back to original prompt");
            return queryText;
        }
        rewritten = rewritten.strip();
        log.info("Query rewritten: '{}' → '{}'", queryText, rewritten);
        return rewritten;
    }
}
