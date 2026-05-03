package com.agenthub.infrastructure.runtime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Runtime配置类。
 * <p>
 * 配置Agent运行时框架选择和相关参数。
 * </p>
 */
@Configuration
public class RuntimeConfiguration {
    
    /**
     * 默认Agent框架类型。
     */
    @Value("${agenthub.runtime.default-framework:GOOGLE_ADK}")
    private String defaultFramework;
    
    /**
     * 是否启用Google ADK框架。
     */
    @Value("${agenthub.runtime.google-adk.enabled:true}")
    private boolean googleAdkEnabled;
    
    /**
     * 是否启用Embabel-Agent框架。
     */
    @Value("${agenthub.runtime.embabel.enabled:true}")
    private boolean embabelEnabled;
    
    @Bean
    public String defaultFramework() {
        return defaultFramework;
    }
    
    public String getDefaultFramework() {
        return defaultFramework;
    }
    
    public boolean isGoogleAdkEnabled() {
        return googleAdkEnabled;
    }
    
    public boolean isEmbabelEnabled() {
        return embabelEnabled;
    }
}
