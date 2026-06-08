package com.agenthub.test.schema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CLI 入口：被 Gradle :generateSchemaDdl 任务调用。
 * 用法：java com.agenthub.test.schema.SchemaFileGeneratorCli [outputDir]
 */
public final class SchemaFileGeneratorCli {

    private static final Logger log = LoggerFactory.getLogger(SchemaFileGeneratorCli.class);

    private SchemaFileGeneratorCli() {
    }

    public static void main(String[] args) throws Exception {
        String outputDir = args.length > 0 ? args[0] : "sql";
        log.info("Generating dialect-specific schema files to: {}", outputDir);
        SchemaFileGenerator.generateAll(outputDir);
        log.info("Done. Generated files for: {}", SchemaFileGenerator.DIALECTS.size() + " dialects");
    }
}
