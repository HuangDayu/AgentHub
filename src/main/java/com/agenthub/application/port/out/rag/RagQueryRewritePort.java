package com.agenthub.application.port.out.rag;

/**
 * 查询改写端口接口。
 * <p>
 * 定义查询改写（拼写修正、同义扩展等）的业务操作契约。
 * </p>
 */
public interface RagQueryRewritePort {

    /**
     * 改写查询文本。
     *
     * @param queryText 原始查询文本
     * @return 改写后的查询文本
     */
    String rewrite(String kbId, String queryText);
}
