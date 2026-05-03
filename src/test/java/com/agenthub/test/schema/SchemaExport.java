package com.agenthub.test.schema;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记需要生成 DDL 的 Entity 类或包。
 */
@Target({ElementType.TYPE, ElementType.PACKAGE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SchemaExport {
    
    /**
     * 是否在生成的 SQL 中包含建表语句。
     */
    boolean createTable() default true;
    
    /**
     * 是否在生成的 SQL 中包含索引语句。
     */
    boolean createIndex() default true;
    
    /**
     * 输出文件路径（相对于项目根目录）。
     */
    String outputPath() default "sql/generated-schema.sql";
}
