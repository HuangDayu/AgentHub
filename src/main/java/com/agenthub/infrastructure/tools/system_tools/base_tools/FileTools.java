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
import java.nio.file.StandardOpenOption;

/**
 * @author huangdayu
 */
@RequiredArgsConstructor
@AgentTools(name = "FileTools", description = "文件操作工具，提供文件的读取、写入、编辑和补丁应用功能")
public class FileTools {

    @Tool(description = "读取指定路径的文件内容")
    public FileContentResult read(@ToolParam String filePath) throws IOException {
        Path path = Paths.get(filePath);
        String content = Files.readString(path);
        return new FileContentResult(true, filePath, content, "文件读取成功", content.length());
    }

    @Tool(description = "将内容写入指定路径的文件")
    public FileContentResult write(@ToolParam String filePath, @ToolParam String content) throws IOException {
        Path path = Paths.get(filePath);
        Files.writeString(path, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        return new FileContentResult(true, filePath, content, "文件写入成功", content.length());
    }

    @Tool(description = "编辑文件，替换指定内容")
    public FileContentResult edit(@ToolParam String filePath, @ToolParam String oldContent, @ToolParam String newContent) throws IOException {
        FileContentResult result = read(filePath);
        String updatedContent = result.getContent().replace(oldContent, newContent);
        return write(filePath, updatedContent);
    }

    @Tool(description = "应用补丁到指定文件")
    public FileContentResult applyPatch(@ToolParam String filePath, @ToolParam String patch) throws IOException {
        FileContentResult result = read(filePath);
        String patchedContent = result.getContent() + "\n" + patch;
        return write(filePath, patchedContent);
    }
}
