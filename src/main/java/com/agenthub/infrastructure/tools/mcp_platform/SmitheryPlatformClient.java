package com.agenthub.infrastructure.tools.mcp_platform;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Smithery 平台客户端，从 smithery.ai 搜索 MCP 工具。
 */
@Slf4j
public class SmitheryPlatformClient implements McpPlatformClient {

    private static final String BASE_URL = "https://api.smithery.ai";
    private static final McpPlatform PLATFORM = new McpPlatform(
            "smithery", "Smithery", "Smithery MCP 工具注册平台",
            BASE_URL + "/v1/packages", BASE_URL + "/v1/packages", "https://smithery.ai"
    );

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public SmitheryPlatformClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
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
            String url = PLATFORM.searchApiUrl() + "?q=" + query + "&pageSize=" + pageSize;
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET,
                    new HttpEntity<>(new HttpHeaders()), String.class);
            return parseSearchResults(response.getBody());
        } catch (Exception e) {
            log.error("Smithery搜索失败: {}", query, e);
            return List.of();
        }
    }

    @Override
    public McpToolInfo getToolDetail(String qualifiedName) {
        try {
            ResponseEntity<String> response = exchangeDetail(qualifiedName);
            return parseToolDetail(response.getBody(), qualifiedName);
        } catch (Exception e) {
            log.error("Smithery获取工具详情失败: {}", qualifiedName, e);
            return null;
        }
    }

    private ResponseEntity<String> exchangeDetail(String qualifiedName) {
        String url = PLATFORM.detailApiUrl() + "/" + qualifiedName;
        return restTemplate.exchange(url, HttpMethod.GET,
                new HttpEntity<>(new HttpHeaders()), String.class);
    }

    @Override
    public String getInstallationCommand(String qualifiedName) {
        return "npx -y @smithery/cli install " + qualifiedName;
    }

    private List<McpToolInfo> parseSearchResults(String responseBody) {
        List<McpToolInfo> results = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode items = root.has("items") ? root.get("items") : root;
            if (items.isArray()) {
                for (JsonNode item : items) {
                    results.add(mapToToolInfo(item));
                }
            }
        } catch (Exception e) {
            log.error("解析Smithery搜索结果失败", e);
        }
        return results;
    }

    private McpToolInfo mapToToolInfo(JsonNode item) {
        McpToolInfo info = new McpToolInfo();
        info.setQualifiedName(getText(item, "qualifiedName"));
        info.setDisplayName(getText(item, "displayName"));
        info.setDescription(getText(item, "description"));
        info.setHomepage(getText(item, "homepage"));
        info.setInstallationUrl(getText(item, "installationUrl"));
        info.setTransportType(getText(item, "transportType"));
        info.setPackageName(getText(item, "packageName"));
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
            log.error("解析Smithery工具详情失败: {}", qualifiedName, e);
            return null;
        }
    }

    private String getText(JsonNode node, String field) {
        return node.has(field) && !node.get(field).isNull() ? node.get(field).asText() : "";
    }
}
