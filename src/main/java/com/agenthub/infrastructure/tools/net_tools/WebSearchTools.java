package com.agenthub.infrastructure.tools.net_tools;

import com.agenthub.infrastructure.tools.annotations.AgentTools;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * @author huangdayu
 */
@RequiredArgsConstructor
@AgentTools(name = "WebSearchTools", description = "网络搜索工具，提供互联网在线搜索服务，支持搜索新闻、文档、事件等")
public class WebSearchTools {

    private final ObjectMapper objectMapper;

    @Tool(description = "互联网在线搜索服务，比如搜索新闻，文档，事件等")
    public String searchWeb(@ToolParam String query) throws IOException, InterruptedException {
        String apiUrl = "https://api.duckduckgo.com/?q=" +
                URLEncoder.encode(query, StandardCharsets.UTF_8) +
                "&format=json&no_html=1";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return extractSearchResults(response.body());
    }

    @SneakyThrows
    private String extractSearchResults(String jsonResponse) {
        StringBuilder results = new StringBuilder();
        JsonNode root = objectMapper.readTree(jsonResponse);
        JsonNode relatedTopics = root.get("RelatedTopics");
        if (relatedTopics != null && relatedTopics.isArray()) {
            for (JsonNode topic : relatedTopics) {
                JsonNode textNode = topic.get("Text");
                if (textNode != null && textNode.isTextual()) {
                    results.append("• ").append(textNode.asText()).append("\n");
                }
            }
        }
        return results.toString();
    }
}