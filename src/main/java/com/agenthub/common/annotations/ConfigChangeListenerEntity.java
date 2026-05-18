package com.agenthub.common.annotations;

import com.agenthub.domain.enums.AgentConfigCategory;
import com.agenthub.domain.enums.AgentConfigType;

import java.lang.annotation.*;

/**
 * 标记需要审计的实体类
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConfigChangeListenerEntity {
    String value() default "";

    AgentConfigCategory category();

    AgentConfigType type();
}
