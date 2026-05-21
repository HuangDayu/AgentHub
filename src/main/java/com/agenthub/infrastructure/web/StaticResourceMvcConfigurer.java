package com.agenthub.infrastructure.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

/**
 * Web 配置 - 处理前端路由。
 */
/**
 * Web MVC 配置。
 * <p>
 * 注册 SPA 前端路由回退过滤器，处理前端路由的 fallback 逻辑。
 */
@Configuration
public class StaticResourceMvcConfigurer implements WebMvcConfigurer {

    /**
     * 添加视图控制器。
     * <p>
     * 配置前端目录的欢迎页转发。
     * </p>
     *
     * @param registry 视图控制器注册表
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/agenthub").setViewName("forward:/agenthub/index.html");
        registry.addViewController("/agenthub/").setViewName("forward:/agenthub/index.html");
    }

    /**
     * 添加资源处理器。
     * <p>
     * 配置SPA前端路由回退和静态资源处理。
     * </p>
     *
     * @param registry 资源处理器注册表
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        addSpaResourceHandler(registry, "/agenthub/**", "classpath:/static/agenthub/", "agenthub");
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/static/assets/");
    }

    /**
     * 添加SPA资源处理器。
     *
     * @param registry     资源处理器注册表
     * @param pathPattern  路径模式
     * @param location     资源位置
     * @param prefix       SPA前缀
     */
    private void addSpaResourceHandler(ResourceHandlerRegistry registry, String pathPattern,
            String location, String prefix) {
        registry.addResourceHandler(pathPattern)
                .addResourceLocations(location)
                .resourceChain(true)
                .addResolver(new SpaPathResolver(prefix));
    }

    /**
     * SPA 路径解析器：对于非文件请求返回 index.html。
     */
    private static class SpaPathResolver extends PathResourceResolver {
        private final String prefix;

        SpaPathResolver(String prefix) {
            this.prefix = prefix;
        }

        @Override
        protected Resource getResource(String resourcePath, Resource location) throws IOException {
            Resource requested = super.getResource(resourcePath, location);
            if (requested != null && requested.exists()) {
                return requested;
            }
            return super.getResource("index.html", location);
        }
    }
}
