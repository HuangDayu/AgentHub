package com.agenthub.infrastructure.tools.net_tools;

import com.agenthub.application.port.out.DocumentFileStoragePort;
import com.agenthub.infrastructure.tools.annotations.AgentTools;
import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 基于 Apache Tika 的网络文件读取工具，自动识别并提取 PDF/DOCX/HTML/JSON/纯文本等格式的文本内容。
 */
@RequiredArgsConstructor
@AgentTools(name = "NetFilesReadTools", description = "网络文件读取工具，基于Tika自动提取PDF/DOCX/HTML/JSON等格式文本内容")
public class NetFilesReadTools {

    private static final Logger log = LoggerFactory.getLogger(NetFilesReadTools.class);
    private static final int MAX_CHARS = 100_000;
    private static final String FALLBACK = "downloaded_file";
    private final DocumentFileStoragePort documentFileStoragePort;
    private final Tika tika = new Tika();

    @Tool(description = "读取网络文件文本内容和本服务缓存的文件(路径以‘agenthub/’开头)，自动识别PDF/DOCX/HTML/JSON/纯文本等格式")
    public String readNetFile(@ToolParam(description = "文件URL") String url) throws Exception {
        log.info("读取网络文件: {}", url);
        try (var in = open(url)) {
            String text = tika.parseToString(in);
            return text.length() > MAX_CHARS ? text.substring(0, MAX_CHARS) + "\n... [截断]" : text;
        }
    }

    @Tool(description = "批量读取多个网络文件，返回URL到内容的映射")
    public Map<String, String> readNetFiles(@ToolParam(description = "URL列表") List<String> urls) {
        Map<String, String> r = new LinkedHashMap<>();
        urls.forEach(u -> {
            try { r.put(u, readNetFile(u)); } catch (Exception e) { r.put(u, "【失败】" + e.getMessage()); log.warn("读取失败: {}", u, e); }
        });
        return r;
    }

    @Tool(description = "下载网络文件原始内容到本地临时目录，返回本地文件路径")
    public String downloadNetFile(@ToolParam(description = "文件URL") String url) throws Exception {
        Path target = Paths.get(System.getProperty("java.io.tmpdir"), "agenthub-tmpfiles", name(url));
        Files.createDirectories(target.getParent());
        try (var in = open(url)) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
        log.info("下载完成: {} ({} 字节)", target, Files.size(target));
        return target.toAbsolutePath().toString();
    }

    private InputStream open(String url) throws Exception {
        if (url.startsWith("agenthub/")) {
            return documentFileStoragePort.retrieve(url);
        }
        var req = HttpRequest.newBuilder(URI.create(url)).GET().build();
        try (HttpClient httpClient = HttpClient.newHttpClient()) {
            return httpClient.send(req, HttpResponse.BodyHandlers.ofInputStream()).body();
        }
    }

    private static String name(String url) {
        try {
            return extractFileName(url);
        } catch (Exception e) {
            return FALLBACK;
        }
    }

    private static String extractFileName(String url) {
        String p = URI.create(url).getPath();
        if (p == null || p.isEmpty() || p.endsWith("/")) return FALLBACK;
        String n = p.substring(p.lastIndexOf('/') + 1);
        int q = n.indexOf('?');
        return n.isEmpty() ? FALLBACK : (q > 0 ? n.substring(0, q) : n);
    }
}
