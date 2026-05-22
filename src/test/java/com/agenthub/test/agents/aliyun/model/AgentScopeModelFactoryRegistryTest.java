package com.agenthub.test.agents.aliyun.model;

import com.agenthub.application.port.out.repositories.ModelConfigRepository;
import com.agenthub.domain.enums.ModelSupplier;
import com.agenthub.domain.model.ModelConfig;
import com.agenthub.infrastructure.agents.aliyun.model.AgentScopeModelFactory;
import com.agenthub.infrastructure.agents.aliyun.model.AgentScopeModelFactoryRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * 验证 {@link AgentScopeModelFactoryRegistry} 的注册与缓存逻辑。
 */
@ExtendWith(MockitoExtension.class)
public class AgentScopeModelFactoryRegistryTest {

    @Mock
    private AgentScopeModelFactory factory;

    @Mock
    private ModelConfigRepository repository;

    @Test
    public void shouldRegisterFactoriesBySupplier() {
        when(factory.getSupplier()).thenReturn(ModelSupplier.OLLAMA);

        var registry = new AgentScopeModelFactoryRegistry(List.of(factory), repository);
        assertThat(registry).isNotNull();
    }

    @Test
    public void shouldThrowWhenNoFactoryForSupplier() {
        var config = ModelConfig.builder()
                .id("cfg-1").supplier(ModelSupplier.OLLAMA)
                .model("llama3").build();

        when(repository.findById("cfg-1")).thenReturn(Optional.of(config));

        var registry = new AgentScopeModelFactoryRegistry(List.of(), repository);

        assertThatThrownBy(() -> registry.getOrCreateModel("cfg-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("OLLAMA");
    }

    @Test
    public void shouldThrowWhenConfigNotFound() {
        when(repository.findById("missing")).thenReturn(Optional.empty());

        var registry = new AgentScopeModelFactoryRegistry(List.of(), repository);

        assertThatThrownBy(() -> registry.getOrCreateModel("missing"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    public void shouldCacheModelInstance() {
        var mockModel = org.mockito.Mockito.mock(io.agentscope.core.model.Model.class);
        when(factory.getSupplier()).thenReturn(ModelSupplier.OLLAMA);

        var config = ModelConfig.builder()
                .id("cfg-1").supplier(ModelSupplier.OLLAMA)
                .apiKey("test-key").model("llama3").build();

        when(repository.findById("cfg-1")).thenReturn(Optional.of(config));
        when(factory.create(config)).thenReturn(mockModel);

        var registry = new AgentScopeModelFactoryRegistry(List.of(factory), repository);

        var result1 = registry.getOrCreateModel("cfg-1");
        var result2 = registry.getOrCreateModel("cfg-1");

        assertThat(result1).isNotNull();
        assertThat(result1).isSameAs(result2);
    }

}
