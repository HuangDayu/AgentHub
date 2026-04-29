package com.agenthub.infrastructure.spring;

import com.agenthub.application.port.out.rag.QueryRewritePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

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
 * @see QueryRewritePort
 * @since 1.0.0
 */
@Component
public class SpringAiQueryRewriteAdapter implements QueryRewritePort {

    private static final Logger log = LoggerFactory.getLogger(SpringAiQueryRewriteAdapter.class);

    /**
     * 查询改写的系统提示词模板。
     * <p>将用户自然语言查询改写为更精确的搜索关键词，保留核心意图。</p>
     */
    private static final String REWRITE_PROMPT_TEMPLATE = """
            你是一个搜索查询优化专家。请将用户的自然语言查询改写为更精确的搜索关键词。
            要求：
            
            0. 不使用深度思考
            1. 保留核心语义和意图
            2. 去除冗余的口语化表达
            3. 添加相关的同义词或技术术语以提高召回率
            4. 仅输出改写后的查询，不要添加任何解释
            5. 改写后的查询长度不超过原文的两倍
            
            原始查询：%s
            改写后的查询：""";

    private final SpringAiFrameworkAdapter springAiFrameworkAdapter;

    public SpringAiQueryRewriteAdapter(SpringAiFrameworkAdapter springAiFrameworkAdapter) {
        this.springAiFrameworkAdapter = springAiFrameworkAdapter;
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
            ChatModel chatModel = springAiFrameworkAdapter.getChatModelByKbId(kbId);
            return callLlmForRewrite(chatModel, queryText);
        } catch (Exception e) {
            log.error("LLM query rewrite failed, falling back to original query: {}", e.getMessage(), e);
            return queryText;
        }
    }

    /**
     * 调用LLM执行改写。
     */
    private String callLlmForRewrite(ChatModel chatModel, String queryText) {
        String promptText = REWRITE_PROMPT_TEMPLATE.formatted(queryText.strip());
        Prompt prompt = new Prompt(promptText);
        log.debug("Sending query rewrite request to LLM, original query: {}", queryText);
        String rewritten = chatModel.call(prompt).getResult().getOutput().getText();
        return validateRewrittenResult(rewritten, queryText);
    }

    /**
     * 验证改写结果。
     */
    private String validateRewrittenResult(String rewritten, String queryText) {
        if (rewritten == null || rewritten.isBlank()) {
            log.warn("LLM returned empty rewrite result, falling back to original query");
            return queryText;
        }
        rewritten = rewritten.strip();
        log.info("Query rewritten: '{}' → '{}'", queryText, rewritten);
        return rewritten;
    }
}
