package com.agenthub.infrastructure.tools.core_tools;

import com.agenthub.infrastructure.tools.annotations.AgentTools;
import com.agenthub.infrastructure.tools.core_tools.dto.WebSearchResult;
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
@AgentTools(name = "WebFetchTools", description = "网络工具，提供网页抓取、搜索和X平台搜索功能")
public class WebFetchTools {

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
        var apiUrl = "https://api.twitter.com/2/tweets/search/recent?query=" + URLEncoder.encode(query, StandardCharsets.UTF_8);
        var request = HttpRequest.newBuilder().uri(URI.create(apiUrl))
                .header("Authorization", "Bearer " + bearerToken).build();
        var body = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString()).body();
        return new WebSearchResult(true, query, List.of(new WebSearchResult.SearchItem("X Search", query, body)), 1, "X平台搜索成功");
    }

    private String buildSearchUrl(String query) {
        return "https://api.duckduckgo.com/?q=" +
                URLEncoder.encode(query, StandardCharsets.UTF_8) +
                "&format=json&no_html=1";
    }

    private List<WebSearchResult.SearchItem> extractSearchResults(String jsonResponse) throws IOException {
        var topics = objectMapper.readTree(jsonResponse).get("RelatedTopics");
        if (topics == null || !topics.isArray()) return List.of();
        List<WebSearchResult.SearchItem> results = new ArrayList<>();
        topics.iterator().forEachRemaining(t -> {
            var text = t.get("Text");
            if (text != null) results.add(new WebSearchResult.SearchItem("Search Result", "", text.asText()));
        });
        return results;
    }
}
