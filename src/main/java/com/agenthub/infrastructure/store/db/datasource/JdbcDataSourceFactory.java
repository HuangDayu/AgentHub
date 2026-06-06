package com.agenthub.infrastructure.store.db.datasource;

import com.agenthub.domain.model.AgentDataSource;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Map;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

/**
 * JDBC 数据源工厂 - 从 AgentDataSource.endpointUri + propertiesJson 创建 HikariDataSource。
 */
@Slf4j
@Component
public class JdbcDataSourceFactory {

    /**
     * 根据 AgentDataSource 创建 DataSource
     */
    public DataSource create(AgentDataSource source) {
        HikariConfig config = baseConfig(source);
        applyProperties(config, source.getPropertiesJson());
        return new HikariDataSource(config);
    }

    private HikariConfig baseConfig(AgentDataSource source) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(source.getEndpointUri());
        config.setMaximumPoolSize(2);
        config.setMinimumIdle(0);
        config.setConnectionTimeout(5_000L);
        config.setIdleTimeout(60_000L);
        config.setPoolName(poolName(source.getId()));
        return config;
    }

    private static String poolName(String id) {
        return "agenthub-ds-" + id;
    }

    private void applyProperties(HikariConfig config, String propertiesJson) {
        if (propertiesJson == null || propertiesJson.isBlank()) return;
        try {
            Map<?, ?> map = parseJson(propertiesJson);
            map.forEach((k, v) -> applyProperty(config, String.valueOf(k), String.valueOf(v)));
        } catch (Exception e) {
            log.warn("failed to parse propertiesJson, ignored", e);
        }
    }

    private void applyProperty(HikariConfig config, String key, String value) {
        if (key == null || key.isBlank()) return;
        dispatchProperty(config, key, value);
    }

    private void dispatchProperty(HikariConfig config, String key, String value) {
        switch (key) {
            case "username", "user" -> config.setUsername(value);
            case "password" -> config.setPassword(value);
            case "driverClassName", "driver" -> config.setDriverClassName(value);
            default -> applyNumericOrPassThrough(config, key, value);
        }
    }

    private void applyNumericOrPassThrough(HikariConfig config, String key, String value) {
        switch (key) {
            case "maximumPoolSize", "maxPoolSize" -> setInt(config::setMaximumPoolSize, value);
            case "minimumIdle", "minIdle" -> setInt(config::setMinimumIdle, value);
            case "connectionTimeout" -> setLong(config::setConnectionTimeout, value);
            case "idleTimeout" -> setLong(config::setIdleTimeout, value);
            default -> config.addDataSourceProperty(key, value);
        }
    }

    private static void setInt(IntConsumer setter, String value) {
        try { setter.accept(Integer.parseInt(value)); } catch (NumberFormatException ignored) { }
    }

    private static void setLong(LongConsumer setter, String value) {
        try { setter.accept(Long.parseLong(value)); } catch (NumberFormatException ignored) { }
    }

    private static Map<?, ?> parseJson(String json) throws JsonProcessingException {
        return new ObjectMapper().readValue(json, Map.class);
    }
}
