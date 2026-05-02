package com.agenthub.application.usecase;

import com.agenthub.domain.model.VectorStoreConfig;
import com.agenthub.application.port.out.VectorPoolManagerPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 向量库管理用例。
 */
@Component
@RequiredArgsConstructor
public class ManageVectorStoreUseCase {
    
    private final VectorStoreConfigUseCase configAppService;
    private final VectorPoolManagerPort vectorStoreManager;

    public void deleteConfigAndClearCache(String configId) {
        VectorStoreConfig config = configAppService.getById(configId);
        configAppService.deleteById(configId);
        vectorStoreManager.destroy(config);
    }

    public void refreshVectorStore(String configId) {
        VectorStoreConfig config = configAppService.getById(configId);
        vectorStoreManager.refresh(config);
    }

    public void destroyInstance(String configId) {
        VectorStoreConfig config = configAppService.getById(configId);
        vectorStoreManager.destroy(config);
    }
}
