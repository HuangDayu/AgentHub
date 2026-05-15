package com.agenthub.infrastructure.web;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * SPA fallback filter: 所有非 API/静态资源请求都返回 index.html，让 Vue Router history 模式正常工作。
 */
@Component
@Order(1)
public class AgentHubWebFilter implements Filter {

    private static final String INDEX_HTML_PATH = "static/index.html";
    private byte[] indexHtmlContent;

    /**
     * 初始化过滤器，加载index.html内容。
     *
     * @param filterConfig 过滤器配置
     * @throws ServletException 加载失败时抛出
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        try {
            loadIndexHtml();
        } catch (IOException e) {
            throw new ServletException("Cannot load index.html", e);
        }
    }

    /**
     * 加载index.html文件内容。
     *
     * @throws IOException 读取失败时抛出
     */
    private void loadIndexHtml() throws IOException {
        ClassPathResource resource = new ClassPathResource(INDEX_HTML_PATH);
        try (InputStream is = resource.getInputStream()) {
            indexHtmlContent = StreamUtils.copyToByteArray(is);
        }
    }

    /**
     * 执行过滤逻辑。
     * <p>
     * API和静态资源请求放行，其他请求返回index.html。
     * </p>
     *
     * @param request  请求对象
     * @param response 响应对象
     * @param chain    过滤器链
     * @throws IOException      IO异常
     * @throws ServletException Servlet异常
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String uri = httpRequest.getRequestURI();

        if (uri.equals("/favicon.ico")) {
            return;
        }

        if (shouldPassThrough(uri)) {
            chain.doFilter(request, response);
            return;
        }
        serveIndexHtml(response);
    }

    /**
     * 判断是否应该放行请求。
     *
     * @param uri 请求URI
     * @return 是否放行
     */
    private boolean shouldPassThrough(String uri) {
        return uri.startsWith("/api/") ||
                uri.startsWith("/actuator") ||
                uri.startsWith("/assets/") ||
                isStaticResource(uri);
    }



    /**
     * 判断是否是静态资源请求。
     *
     * @param uri 请求URI
     * @return 是否是静态资源
     */
    private boolean isStaticResource(String uri) {
        return uri.endsWith(".js") || uri.endsWith(".css") || uri.endsWith(".svg") ||
                uri.endsWith(".png") || uri.endsWith(".jpg") || uri.endsWith(".ico") ||
                uri.endsWith(".woff") || uri.endsWith(".woff2") || uri.endsWith(".ttf");
    }

    /**
     * 返回index.html内容。
     *
     * @param response 响应对象
     * @throws IOException 写入失败时抛出
     */
    private void serveIndexHtml(ServletResponse response) throws IOException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        httpResponse.setContentType("text/html; charset=UTF-8");
        httpResponse.setContentLength(indexHtmlContent.length);
        httpResponse.getOutputStream().write(indexHtmlContent);
    }
}
