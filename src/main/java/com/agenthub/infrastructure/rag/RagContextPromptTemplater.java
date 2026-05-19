package com.agenthub.infrastructure.rag;

/**
 * @author huangdayu
 */
public class RagContextPromptTemplater {

    /**
     * 查询改写的系统提示词模板。
     * <p>将用户自然语言查询改写为更精确的搜索关键词，保留核心意图。</p>
     */
    public static final String REWRITE_PROMPT_TEMPLATE = """
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

    public static final String KEYWORDS_PROMPT_TEMPLATE = """
            你是一个关键词提取专家。请从用户的自然语言查询中提取关键词。
            要求：
            
            0. 提取关键词时，请勿使用深度思考
            1. 提取的关键词数量不超过 %s 个
            """;


    public static final String TRANSLATION_PROMPT_TEMPLATE = """
            你是一个翻译专家。请将用户的自然语言查询翻译为中文。
            要求：
            
            0. 翻译时，请勿使用深度思考
            """;

    public static final String COMPRESSION_PROMPT_TEMPLATE = """
            你是一个查询压缩专家。请将用户的自然语言查询压缩为更简洁的查询。
            要求：
            
            0. 压缩时，请勿使用深度思考
            """;

}
