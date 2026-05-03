package com.agenthub.infrastructure.tools;

import com.agenthub.infrastructure.tools.annotations.AgentTool;
import com.agenthub.infrastructure.tools.annotations.ToolParameter;
import org.springframework.stereotype.Component;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class HttpRequestTools {
    
    private final HttpClient client = HttpClient.newHttpClient();
    
    @AgentTool(name = "http_get", description = "Send HTTP GET request", tags = {"http", "network"})
    public String httpGet(@ToolParameter(name = "url") String url) throws Exception {
        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        return resp.body();
    }
    
    @AgentTool(name = "http_post", description = "Send HTTP POST request", tags = {"http", "network"})
    public String httpPost(@ToolParameter(name = "url") String url,
                          @ToolParameter(name = "body") String body) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        return resp.body();
    }
}
