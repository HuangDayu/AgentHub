package com.agenthub.infrastructure.tools.mcp_platform;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Glama 平台客户端，从 glama.ai 搜索 MCP 工具。
 */
@Slf4j
public class GlamaPlatformClient implements McpPlatformClient {

    private static final String BASE_URL = "https://glama.ai";
    private static final McpPlatform PLATFORM = new McpPlatform(
            "glama", "Glama", "Glama MCP 工具注册平台",
            BASE_URL + "/api/mcp/servers", BASE_URL + "/api/mcp/servers", BASE_URL + "/mcp/servers"
    );

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GlamaPlatformClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public McpPlatform getPlatform() {
        return PLATFORM;
    }

    @Override
    public List<McpToolInfo> searchTools(String query, int pageSize) {
        try {
            String url = PLATFORM.searchApiUrl() + "?query=" + query + "&limit=" + pageSize;
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET,
                    new HttpEntity<>(new HttpHeaders()), String.class);
            return parseSearchResults(response.getBody());
        } catch (Exception e) {
            log.error("Glama搜索失败: {}", query, e);
            return List.of();
        }
    }

    @Override
    public McpToolInfo getToolDetail(String qualifiedName) {
        try {
            return fetchToolDetail(qualifiedName);
        } catch (Exception e) {
            log.error("Glama获取工具详情失败: {}", qualifiedName, e);
            return null;
        }
    }

    /**
     * 执行 Glama API 请求并解析工具详情。
     */
    private McpToolInfo fetchToolDetail(String qualifiedName) {
        String url = PLATFORM.detailApiUrl() + "/" + qualifiedName;
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET,
                new HttpEntity<>(new HttpHeaders()), String.class);
        return parseToolDetail(response.getBody(), qualifiedName);
    }

    @Override
    public String getInstallationCommand(String qualifiedName) {
        return "npx -y @glama/mcp install " + qualifiedName;
    }

    private List<McpToolInfo> parseSearchResults(String responseBody) {
        List<McpToolInfo> results = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode items = root.has("servers") ? root.get("servers") : root;
            if (items.isArray()) {
                for (JsonNode item : items) {
                    results.add(mapToToolInfo(item));
                }
            }
        } catch (Exception e) {
            log.error("解析Glama搜索结果失败", e);
        }
        return results;
    }

    private McpToolInfo mapToToolInfo(JsonNode item) {
        McpToolInfo info = new McpToolInfo();
        info.setQualifiedName(getText(item, "slug"));
        info.setDisplayName(getText(item, "name"));
        info.setDescription(getText(item, "description"));
        info.setHomepage(getText(item, "homepage"));
        info.setTransportType(getText(item, "transportType"));
        info.setPlatformId(PLATFORM.id());
        return info;
    }

    private McpToolInfo parseToolDetail(String responseBody, String qualifiedName) {
        try {
            JsonNode item = objectMapper.readTree(responseBody);
            McpToolInfo info = mapToToolInfo(item);
            info.setQualifiedName(qualifiedName);
            return info;
        } catch (Exception e) {
            log.error("解析Glama工具详情失败: {}", qualifiedName, e);
            return null;
        }
    }

    private String getText(JsonNode node, String field) {
        return node.has(field) && !node.get(field).isNull() ? node.get(field).asText() : "";
    }
}
