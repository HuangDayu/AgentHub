package com.agenthub.infrastructure.vector;

import com.agenthub.domain.enums.VectorStoreType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * VectorStore 工厂注册表。
 * 自动收集所有 VectorStoreFactory 实现，并按类型注册。
 */
@Component
public class VectorStoreFactoryRegistry {

    private final Map<String, VectorStoreFactory> factories;

    public VectorStoreFactoryRegistry(List<VectorStoreFactory> factoryList) {
        this.factories = factoryList.stream()
                .collect(Collectors.toMap(f -> f.getType(), Function.identity()));
    }

    public VectorStoreFactory getFactory(VectorStoreType type) {
        VectorStoreFactory factory = factories.get(type.name());
        if (factory == null) {
            throw new IllegalArgumentException("Unsupported vector store type: " + type +
                    ". Supported types: " + String.join(", ", factories.keySet()));
        }
        return factory;
    }

    public boolean supports(VectorStoreType type) {
        return factories.containsKey(type.name());
    }

    public List<String> supportedTypes() {
        return List.copyOf(factories.keySet());
    }
}
