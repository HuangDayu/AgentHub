package com.agenthub.test.agents.aliyun.memory;

import com.agenthub.infrastructure.agents.aliyun.memory.MemoryConfigFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 测试 {@link MemoryConfigFactory} 的功能。
 */
public class MemoryConfigFactoryTest {

    private MemoryConfigFactory factory;

    @BeforeEach
    void setUp() {
        factory = new MemoryConfigFactory();
    }

    @Test
    void shouldCreateDefaultCompactionConfig() {
        var config = factory.createDefaultCompactionConfig();
        assertThat(config).isNotNull();
    }

    @Test
    void shouldCreateCustomCompactionConfig() {
        var config = factory.createCompactionConfig(50, 20, false);
        assertThat(config).isNotNull();
    }

    @Test
    void shouldCreateDefaultToolResultEvictionConfig() {
        var config = factory.createDefaultToolResultEvictionConfig();
        assertThat(config).isNotNull();
    }

    @Test
    void shouldCreateCustomToolResultEvictionConfig() {
        var config = factory.createToolResultEvictionConfig(100000, 5000);
        assertThat(config).isNotNull();
    }
}
