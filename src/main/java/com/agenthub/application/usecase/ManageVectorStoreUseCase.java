package com.agenthub.application.usecase;

import com.agenthub.domain.model.VectorStoreConfig;
import com.agenthub.application.port.out.rag.VectorStoreManagerPort;
import org.springframework.stereotype.Service;

/**
 * 向量库管理用例。
 */
@Service
public class ManageVectorStoreUseCase {
    
    private final VectorStoreConfigUseCase configAppService;
    private final VectorStoreManagerPort vectorStoreManager;

    public ManageVectorStoreUseCase(
            VectorStoreConfigUseCase configAppService,
            VectorStoreManagerPort vectorStoreManager) {
        this.configAppService = configAppService;
        this.vectorStoreManager = vectorStoreManager;
    }

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
