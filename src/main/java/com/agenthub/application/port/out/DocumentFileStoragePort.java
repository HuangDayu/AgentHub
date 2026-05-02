package com.agenthub.application.port.out;

import java.io.InputStream;

/**
 * 文档存储端口接口。
 */
public interface DocumentFileStoragePort {

    /**
     * 存储文档到指定路径。
     *
     * @param path    存储路径
     * @param content 文档输入流
     * @param size    文档大小（字节）
     * @return 实际存储路径
     */
    String store(String path, InputStream content, long size);

    /**
     * 从指定路径检索文档。
     *
     * @param path 存储路径
     * @return 文档输入流
     */
    InputStream retrieve(String path);

    /**
     * 删除指定路径的文档。
     *
     * @param path 存储路径
     */
    void delete(String path);
}