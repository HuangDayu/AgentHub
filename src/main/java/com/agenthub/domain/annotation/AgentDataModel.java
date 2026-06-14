package com.agenthub.domain.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记可被 Agent 操作的数据模型。
 * <p>
 * 被此注解标记的实体类将自动生成通用 CRUD 工具，
 * Agent 可以通过元数据驱动的方式查询和操作数据。
 * </p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AgentDataModel {

    /**
     * 数据模型名称（Agent 看到的名称）
     */
    String name();

    /**
     * 数据模型描述（Agent 看到的描述）
     */
    String description();

    /**
     * 所属领域（用于分组显示）
     */
    String domain() default "通用";

    /**
     * 图标名称（可选，用于 UI 展示）
     */
    String icon() default "table";

    /**
     * 是否支持创建操作
     */
    boolean creatable() default true;

    /**
     * 是否支持更新操作
     */
    boolean updatable() default true;

    /**
     * 是否支持删除操作
     */
    boolean deletable() default true;

    /**
     * 关联的 Mapper 类（直接操作数据库）
     */
    Class<?> mapper();

    /**
     * 租户字段名（用于租户隔离，为空则不做租户过滤）
     */
    String tenantField() default "tenantId";

    /**
     * 工作空间字段名（用于工作空间隔离，为空则不做工作空间过滤）
     */
    String workspaceField() default "workspaceId";
}
