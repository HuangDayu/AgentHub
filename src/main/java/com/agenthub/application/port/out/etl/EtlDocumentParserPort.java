package com.agenthub.application.port.out.etl;

import com.agenthub.domain.model.etl.DocumentContent;

import java.io.InputStream;

/**
 * 文档解析端口接口。
 */
public interface EtlDocumentParserPort {

    /**
     * 解析文档，提取文本内容。
     *
     * @param documentId  文档ID
     * @param content     文档输入流
     * @param contentType 内容类型（MIME）
     * @param fileName    文件名
     * @return 解析后的文档内容
     */
    DocumentContent parse(String documentId, InputStream content, String contentType, String fileName);
}