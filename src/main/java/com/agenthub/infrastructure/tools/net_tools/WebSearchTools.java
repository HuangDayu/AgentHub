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
                URLEncoder.encode(query, StandardCharsets.UTF_8) + "&format=json&no_html=1";
        String body = httpGet(apiUrl);
        return extractSearchResults(body);
    }

    private String httpGet(String url) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
        return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
    }

    @SneakyThrows
    private String extractSearchResults(String jsonResponse) {
        JsonNode root = objectMapper.readTree(jsonResponse);
        JsonNode topics = root.get("RelatedTopics");
        if (topics == null || !topics.isArray()) return "";
        StringBuilder results = new StringBuilder();
        topics.forEach(t -> {
            JsonNode text = t.get("Text");
            if (text != null && text.isTextual()) results.append("• ").append(text.asText()).append("\n");
        });
        return results.toString();
    }
}