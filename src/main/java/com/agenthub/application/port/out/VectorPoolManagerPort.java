package com.agenthub.application.port.out;

import com.agenthub.application.dto.VectorStoreTestOutput;
import com.agenthub.domain.model.VectorStoreConfig;

/**
 * 向量库管理器端口。
 */
public interface VectorPoolManagerPort {
    
    void refresh(VectorStoreConfig config);
    
    boolean destroy(VectorStoreConfig config);
    
    int destroyAllForTenant(String tenantId);
    
    /**
     * 测试向量库连接。
     * 
     * @param config 向量库配置
     * @return 测试结果数组 [success, message, details]
     */
    VectorStoreTestOutput testConnection(VectorStoreConfig config);

    VectorStoreTestOutput testConnection(String configId);
}
