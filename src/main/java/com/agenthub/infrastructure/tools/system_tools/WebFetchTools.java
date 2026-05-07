package com.agenthub.infrastructure.tools.system_tools;

import com.agenthub.infrastructure.tools.system_tools.annotations.AgentTools;
import org.springframework.ai.tool.annotation.Tool;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@AgentTools(name = "WebFetchTools", description = "网页获取工具，提供网页内容获取、下载、链接提取、HTML解析等功能")
public class WebFetchTools {

    private final HttpClient client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();

    @Tool(name = "web_search", description = "Search the web")
    public String webSearch(String query) throws Exception {
        String url = "https://api.duckduckgo.com/?q=" + query + "&format=json";
        return webFetch(url);
    }

    @Tool(name = "web_fetch", description = "Fetch content from URL")
    public String webFetch(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(30))
            .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    @Tool(name = "web_fetch_with_headers", description = "Fetch URL with custom headers")
    public String webFetchWithHeaders(String url, String headers) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(30));
        for (String header : headers.split(",")) {
            String[] parts = header.split(":");
            if (parts.length == 2) builder.header(parts[0].trim(), parts[1].trim());
        }
        HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    @Tool(name = "web_post", description = "POST data to URL")
    public String webPost(String url, String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .timeout(Duration.ofSeconds(30))
            .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    @Tool(name = "web_get_status", description = "Get HTTP status code for URL")
    public int webGetStatus(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(10))
            .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.statusCode();
    }

    @Tool(name = "web_head", description = "Get headers from URL")
    public String webHead(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .method("HEAD", HttpRequest.BodyPublishers.noBody())
            .timeout(Duration.ofSeconds(10))
            .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.headers().map().toString();
    }

    @Tool(name = "x_search", description = "Search X/Twitter")
    public String xSearch(String query) throws Exception {
        String url = "https://api.twitter.com/2/tweets/search/recent?query=" + query;
        return "X search requires authentication. Query: " + query;
    }

    @Tool(name = "web_download", description = "Download file from URL")
    public String webDownload(String url, String outputPath) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofMinutes(5))
            .build();
        HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
        java.nio.file.Files.write(java.nio.file.Paths.get(outputPath), response.body());
        return "Downloaded to: " + outputPath + " (" + response.body().length + " bytes)";
    }

    @Tool(name = "web_is_reachable", description = "Check if URL is reachable")
    public boolean webIsReachable(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(5))
                .build();
            client.send(request, HttpResponse.BodyHandlers.ofString());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Tool(name = "web_extract_text", description = "Extract text from HTML")
    public String webExtractText(String html) {
        return html.replaceAll("<[^>]+>", " ")
            .replaceAll("\\s+", " ")
            .trim();
    }

    @Tool(name = "web_extract_links", description = "Extract links from HTML")
    public String webExtractLinks(String html) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("href=\"([^\"]+)\"");
        java.util.regex.Matcher matcher = pattern.matcher(html);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) sb.append(matcher.group(1)).append("\n");
        return sb.toString();
    }

    @Tool(name = "web_extract_images", description = "Extract image URLs from HTML")
    public String webExtractImages(String html) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("src=\"([^\"]+\\.(jpg|png|gif|webp))\"");
        java.util.regex.Matcher matcher = pattern.matcher(html);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) sb.append(matcher.group(1)).append("\n");
        return sb.toString();
    }
}
