package com.agenthub.test.schema;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Schema 生成器配置属性。
 */
@ConfigurationProperties(prefix = "schema.generator")
public class SchemaGeneratorProperties {
    
    /**
     * 要扫描的基础包路径。
     */
    private String basePackage = "com.agenthub";
    
    /**
     * schema.sql 输出路径。
     */
    private String schemaOutputPath = "sql/schema.sql";
    
    /**
     * checklist.sql 输出路径。
     */
    private String checklistOutputPath = "sql/checklist.sql";
    
    /**
     * 是否在应用启动时自动生成。
     */
    private boolean autoGenerateOnStartup = false;
    
    public String getBasePackage() {
        return basePackage;
    }
    
    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }
    
    public String getSchemaOutputPath() {
        return schemaOutputPath;
    }
    
    public void setSchemaOutputPath(String schemaOutputPath) {
        this.schemaOutputPath = schemaOutputPath;
    }
    
    public String getChecklistOutputPath() {
        return checklistOutputPath;
    }
    
    public void setChecklistOutputPath(String checklistOutputPath) {
        this.checklistOutputPath = checklistOutputPath;
    }
    
    public boolean isAutoGenerateOnStartup() {
        return autoGenerateOnStartup;
    }
    
    public void setAutoGenerateOnStartup(boolean autoGenerateOnStartup) {
        this.autoGenerateOnStartup = autoGenerateOnStartup;
    }
}
