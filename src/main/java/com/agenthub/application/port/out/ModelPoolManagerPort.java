package com.agenthub.application.port.out;

import com.agenthub.application.dto.ModelTestOutput;

/**
 * 模型管理器端口。
 */
public interface ModelPoolManagerPort {


    /**
     * 清除指定配置的缓存。
     */
    void evictCache(String configId);

    /**
     * 清除所有缓存。
     */
    void evictAllCache();


    /**
     * 测试模型配置。
     */
    ModelTestOutput testModel(String configId);
}
