package com.agenthub.infrastructure.tools.system_tools.base_tools;

import com.agenthub.infrastructure.tools.system_tools.annotations.AgentTools;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

/**
 * @author huangdayu
 */
@RequiredArgsConstructor
@AgentTools(name = "MediaTools", description = "媒体工具，提供文本转语音、图像处理、图像生成、音乐生成和视频生成功能")
public class MediaTools {

    @Tool(description = "文本转语音")
    public String tts(@ToolParam String text, @ToolParam String voice) throws IOException, InterruptedException {
        String apiUrl = "https://api.example.com/tts?text=" + text + "&voice=" + voice;
        return callExternalApi(apiUrl);
    }

    @Tool(description = "图像处理")
    public String image(@ToolParam String imageUrl, @ToolParam String operation, @ToolParam Map<String, Object> params) {
        return "图像处理完成: " + operation + " on " + imageUrl;
    }

    @Tool(description = "生成图像")
    public String imageGenerate(@ToolParam String prompt, @ToolParam String size) throws IOException, InterruptedException {
        String apiUrl = "https://api.example.com/image/generate?prompt=" + prompt + "&size=" + size;
        return callExternalApi(apiUrl);
    }

    @Tool(description = "生成音乐")
    public String musicGenerate(@ToolParam String prompt, @ToolParam String style) throws IOException, InterruptedException {
        String apiUrl = "https://api.example.com/music/generate?prompt=" + prompt + "&style=" + style;
        return callExternalApi(apiUrl);
    }

    @Tool(description = "生成视频")
    public String videoGenerate(@ToolParam String prompt, @ToolParam String duration) throws IOException, InterruptedException {
        String apiUrl = "https://api.example.com/video/generate?prompt=" + prompt + "&duration=" + duration;
        return callExternalApi(apiUrl);
    }

    private String callExternalApi(String apiUrl) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(apiUrl)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}
