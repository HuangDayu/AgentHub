package com.agenthub.domain.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记数据模型字段的 Agent 可见属性。
 * <p>
 * 用于控制字段在 Agent 视图中的显示和行为，
 * 包括是否可见、是否可过滤、是否必填等。
 * </p>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AgentDataField {

    /**
     * 字段描述（Agent 看到的说明）
     */
    String description() default "";

    /**
     * 是否在列表查询中显示
     */
    boolean listVisible() default true;

    /**
     * 是否在详情查询中显示
     */
    boolean detailVisible() default true;

    /**
     * 是否可作为过滤条件
     */
    boolean filterable() default false;

    /**
     * 是否可排序
     */
    boolean sortable() default false;

    /**
     * 创建时是否必填
     */
    boolean required() default false;

    /**
     * 是否从 Agent 视图中隐藏（如 tenantId、workspaceId 等上下文字段）
     */
    boolean hidden() default false;

    /**
     * 枚举类型，自动提取枚举名称作为可选值。
     * <p>
     * 优先级高于 enumValues。设置后自动从枚举类中提取所有常量名。
     * </p>
     */
    @SuppressWarnings("rawtypes")
    Class<? extends Enum> enumType() default Enum.class;

    /**
     * 枚举值说明，格式：{"VALUE1", "VALUE2"} 或 {"VALUE1:说明1", "VALUE2:说明2"}
     * <p>
     * 当 enumType 未设置时使用。建议优先使用 enumType。
     * </p>
     */
    String[] enumValues() default {};

    /**
     * 是否敏感字段（如 apiKey），读取时需要脱敏
     */
    boolean sensitive() default false;
}
