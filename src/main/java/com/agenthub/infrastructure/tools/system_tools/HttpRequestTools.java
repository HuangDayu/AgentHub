package com.agenthub.infrastructure.tools.system_tools;

import com.agenthub.infrastructure.tools.system_tools.annotations.AgentTools;
import org.springframework.ai.tool.annotation.Tool;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@AgentTools(name = "HttpRequestTools", description = "HTTP请求工具，提供HTTP GET、POST等基础HTTP请求发送功能")
public class HttpRequestTools {

    private final HttpClient client = HttpClient.newHttpClient();

    @Tool(name = "http_get", description = "Send HTTP GET request")
    public String httpGet(String url) throws Exception {
        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        return resp.body();
    }

    @Tool(name = "http_post", description = "Send HTTP POST request")
    public String httpPost(String url,
                           String body) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        return resp.body();
    }
}
