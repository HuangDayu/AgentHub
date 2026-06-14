package com.agenthub.infrastructure.tools.core_tools;

import com.agenthub.infrastructure.tools.annotations.AgentTools;
import com.agenthub.infrastructure.tools.core_tools.dto.FileContentResult;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件操作工具，提供文件的读取、写入、编辑、追加和目录操作功能。
 */
@RequiredArgsConstructor
@AgentTools(name = "FileTools", description = "文件操作工具，提供文件的读取、写入、编辑、追加和目录操作功能")
public class FileTools {

    @Tool(description = "读取指定路径的文件内容")
    public FileContentResult read(
            @ToolParam(description = "文件的完整路径") String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) return new FileContentResult(false, filePath, "", "文件不存在", 0);
        String content = Files.readString(path);
        return new FileContentResult(true, filePath, content, "文件读取成功", content.length());
    }

    @Tool(description = "将内容写入指定路径的文件（覆盖已有内容）")
    public FileContentResult write(
            @ToolParam(description = "文件的完整路径") String filePath,
            @ToolParam(description = "要写入的内容") String content) throws IOException {
        Path path = Paths.get(filePath);
        createParentDirs(path);
        Files.writeString(path, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        return new FileContentResult(true, filePath, content, "文件写入成功", content.length());
    }

    @Tool(description = "编辑文件，将文件中的指定内容替换为新内容（仅替换第一个匹配项）")
    public FileContentResult edit(
            @ToolParam(description = "文件的完整路径") String filePath,
            @ToolParam(description = "要被替换的旧内容") String oldContent,
            @ToolParam(description = "替换后的新内容") String newContent) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) return new FileContentResult(false, filePath, "", "文件不存在", 0);
        String content = Files.readString(path);
        if (!content.contains(oldContent)) return new FileContentResult(false, filePath, content, "未找到要替换的内容", 0);
        String updated = content.replaceFirst(java.util.regex.Pattern.quote(oldContent),
                java.util.regex.Matcher.quoteReplacement(newContent));
        Files.writeString(path, updated, StandardOpenOption.TRUNCATE_EXISTING);
        return new FileContentResult(true, filePath, updated, "文件编辑成功", updated.length());
    }

    @Tool(description = "在文件末尾追加内容")
    public FileContentResult append(
            @ToolParam(description = "文件的完整路径") String filePath,
            @ToolParam(description = "要追加的内容") String content) throws IOException {
        Path path = Paths.get(filePath);
        createParentDirs(path);
        Files.writeString(path, content, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        String full = Files.readString(path);
        return new FileContentResult(true, filePath, full, "内容追加成功", full.length());
    }

    @Tool(description = "删除指定路径的文件")
    public String delete(
            @ToolParam(description = "要删除的文件路径") String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) return "文件不存在: " + filePath;
        Files.delete(path);
        return "文件已删除: " + filePath;
    }

    @Tool(description = "列出目录下的文件和子目录")
    public String listDirectory(
            @ToolParam(description = "目录路径") String dirPath) throws IOException {
        Path path = Paths.get(dirPath);
        if (!Files.exists(path)) return "目录不存在: " + dirPath;
        if (!Files.isDirectory(path)) return "路径不是目录: " + dirPath;
        return Files.list(path).sorted((a, b) -> {
            boolean aDir = Files.isDirectory(a);
            boolean bDir = Files.isDirectory(b);
            return aDir != bDir ? (aDir ? -1 : 1)
                    : a.getFileName().toString().compareTo(b.getFileName().toString());
        }).map(this::formatEntry).collect(Collectors.joining("\n"));
    }

    @Tool(description = "检查文件或目录是否存在")
    public boolean exists(@ToolParam(description = "文件或目录路径") String path) {
        return Files.exists(Paths.get(path));
    }

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

    private String formatEntry(Path p) {
        String name = p.getFileName().toString();
        if (Files.isDirectory(p)) return name + "/";
        try { return name + " (" + Files.size(p) + " bytes)"; }
        catch (IOException e) { return name; }
    }

    private void createParentDirs(Path path) throws IOException {
        Path parent = path.getParent();
        if (parent != null && !Files.exists(parent)) Files.createDirectories(parent);
    }
}
