package com.agenthub.application.port.out.rag;

import com.agenthub.domain.model.DocumentContent;

/**
 * 内容清洗端口接口。
 */
public interface DocumentCleanerPort {

    /**
     * 清洗文档内容，去除无关标记和噪声。
     *
     * @param content 待清洗的文档内容
     * @return 清洗后的文档内容
     */
    DocumentContent clean(DocumentContent content);
}