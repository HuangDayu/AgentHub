package com.agenthub.test.schema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Schema 生成器应用启动器。
 */
@Component
@ConditionalOnProperty(name = "schema.generator.auto-generate-on-startup", havingValue = "true")
public class SchemaGeneratorRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SchemaGeneratorRunner.class);

    private final SchemaGeneratorProperties properties;

    /**
     * 构造函数。
     */
    public SchemaGeneratorRunner(SchemaGeneratorProperties properties) {
        this.properties = properties;
    }

    /**
     * 执行生成。
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("=== Starting automatic schema generation ===");
        try {
            executeGeneration();
            log.info("=== Schema generation completed successfully ===");
        } catch (Exception e) {
            log.error("=== Schema generation failed ===", e);
            throw e;
        }
    }

    /**
     * 执行生成逻辑。
     */
    private void executeGeneration() throws Exception {
        SchemaGenerator generator = new SchemaGenerator();
        generator.generate(
                properties.getBasePackage(),
                properties.getSchemaOutputPath(),
                properties.getChecklistOutputPath()
        );
    }
}
