package com.agenthub.infrastructure.tools.system_tools.base_tools;

import com.agenthub.infrastructure.tools.system_tools.annotations.AgentTools;
import com.agenthub.infrastructure.tools.system_tools.base_tools.dto.WebSearchResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author huangdayu
 */
@RequiredArgsConstructor
@AgentTools(name = "WebTools", description = "网络工具，提供网页抓取、搜索和X平台搜索功能")
public class WebTools {

    private final ObjectMapper objectMapper;

    @Tool(description = "抓取网页内容")
    public WebSearchResult webFetch(@ToolParam String url) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<WebSearchResult.SearchItem> items = new ArrayList<>();
        items.add(new WebSearchResult.SearchItem("Web Content", url, response.body()));
        return new WebSearchResult(true, url, items, 1, "网页抓取成功");
    }

    @Tool(description = "网络搜索")
    public WebSearchResult webSearch(@ToolParam String query) throws IOException, InterruptedException {
        String apiUrl = buildSearchUrl(query);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(apiUrl)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<WebSearchResult.SearchItem> results = extractSearchResults(response.body());
        return new WebSearchResult(true, query, results, results.size(), "搜索成功");
    }

    @Tool(description = "X平台(Twitter)搜索")
    public WebSearchResult xSearch(@ToolParam String query, @ToolParam String bearerToken) throws IOException, InterruptedException {
        String apiUrl = "https://api.twitter.com/2/tweets/search/recent?query=" +
                URLEncoder.encode(query, StandardCharsets.UTF_8);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Authorization", "Bearer " + bearerToken)
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<WebSearchResult.SearchItem> items = new ArrayList<>();
        items.add(new WebSearchResult.SearchItem("X Search", query, response.body()));
        return new WebSearchResult(true, query, items, 1, "X平台搜索成功");
    }

    private String buildSearchUrl(String query) {
        return "https://api.duckduckgo.com/?q=" +
                URLEncoder.encode(query, StandardCharsets.UTF_8) +
                "&format=json&no_html=1";
    }

    private List<WebSearchResult.SearchItem> extractSearchResults(String jsonResponse) throws IOException {
        List<WebSearchResult.SearchItem> results = new ArrayList<>();
        JsonNode root = objectMapper.readTree(jsonResponse);
        JsonNode relatedTopics = root.get("RelatedTopics");
        if (relatedTopics != null && relatedTopics.isArray()) {
            for (JsonNode topic : relatedTopics) {
                JsonNode textNode = topic.get("Text");
                if (textNode != null && textNode.isTextual()) {
                    results.add(new WebSearchResult.SearchItem("Search Result", "", textNode.asText()));
                }
            }
        }
        return results;
    }
}
