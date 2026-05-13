package com.agenthub.infrastructure.tools.system_tools.base_tools;

import com.agenthub.infrastructure.tools.system_tools.annotations.AgentTools;
import com.agenthub.infrastructure.tools.system_tools.base_tools.dto.FileContentResult;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author huangdayu
 */
@RequiredArgsConstructor
@AgentTools(name = "FileTransferTools", description = "文件传输工具，提供目录获取、目录列表、文件获取和文件写入功能")
public class FileTransferTools {

    @Tool(description = "获取目录内容")
    public List<String> directoryFetch(@ToolParam String directoryPath) throws IOException {
        Path path = Paths.get(directoryPath);
        return Files.list(path).map(Path::toString).collect(Collectors.toList());
    }

    @Tool(description = "列出目录内容")
    public List<String> directoryList(@ToolParam String directoryPath, @ToolParam boolean recursive) throws IOException {
        Path path = Paths.get(directoryPath);
        int maxDepth = recursive ? Integer.MAX_VALUE : 1;
        return Files.walk(path, maxDepth).map(Path::toString).collect(Collectors.toList());
    }

    @Tool(description = "获取文件内容")
    public FileContentResult fileFetch(@ToolParam String filePath) throws IOException {
        Path path = Paths.get(filePath);
        String content = Files.readString(path);
        return new FileContentResult(true, filePath, content, "文件获取成功", content.length());
    }

    @Tool(description = "写入文件内容")
    public FileContentResult fileWrite(@ToolParam String filePath, @ToolParam String content, @ToolParam boolean append) throws IOException {
        Path path = Paths.get(filePath);
        if (append) {
            Files.writeString(path, content, java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
        } else {
            Files.writeString(path, content);
        }
        return new FileContentResult(true, filePath, content, "文件写入成功", content.length());
    }
}
