package com.agenthub.test.schema;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

/**
 * Schema 生成器自动配置类。
 */
@AutoConfiguration
@ComponentScan("com.agenthub.test.schema")
@EnableConfigurationProperties(SchemaGeneratorProperties.class)
public class SchemaManagerAutoConfiguration {
}
