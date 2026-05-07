package com.agenthub.infrastructure.tools.system_tools.annotations;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @author huangdayu
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface AgentTools {

    boolean defaultEnable() default true;

    String name() default "";

    String description() default "";

}
