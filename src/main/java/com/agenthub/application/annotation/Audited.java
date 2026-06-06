package com.agenthub.application.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自动审计注解 - 标注在 UseCase 方法上自动写入全局审计日志
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audited {
    /** 资源类型 - 对应 AuditResourceType 字符串 */
    String resourceType();

    /** 操作 - 留空时从方法名推断 */
    String action() default "";

    /** 是否记录请求参数 */
    boolean includeArgs() default true;

    /** 是否记录返回值 */
    boolean includeResult() default false;

    /** 是否脱敏 */
    boolean maskSensitive() default true;
}
