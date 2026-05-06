package com.agenthub.infrastructure.tools.function_tools;

import com.agenthub.infrastructure.tools.function_tools.annotations.AgentTools;
import org.springframework.ai.tool.annotation.Tool;

import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;

@AgentTools(name = "FileReadWriteTools", description = "文件读写工具，提供文件读取、写入、追加、补丁、文件系统操作等功能")
public class FileReadWriteTools {

    @Tool(name = "read", description = "Read file content")
    public String read(String path) throws Exception {
        return Files.readString(Paths.get(path));
    }

    @Tool(name = "read_lines", description = "Read specific lines from file")
    public String readLines(String path, int startLine, int endLine) throws Exception {
        List<String> lines = Files.readAllLines(Paths.get(path));
        return lines.subList(startLine, Math.min(endLine, lines.size()))
            .stream().collect(Collectors.joining("\n"));
    }

    @Tool(name = "read_bytes", description = "Read file as bytes")
    public String readBytes(String path) throws Exception {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    @Tool(name = "write", description = "Write content to file")
    public String write(String path, String content) throws Exception {
        Files.writeString(Paths.get(path), content);
        return "Written to: " + path;
    }

    @Tool(name = "write_bytes", description = "Write bytes to file")
    public String writeBytes(String path, String hexBytes) throws Exception {
        byte[] bytes = new byte[hexBytes.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hexBytes.substring(2 * i, 2 * i + 2), 16);
        }
        Files.write(Paths.get(path), bytes);
        return "Written " + bytes.length + " bytes to: " + path;
    }

    @Tool(name = "append", description = "Append content to file")
    public String append(String path, String content) throws Exception {
        Files.writeString(Paths.get(path), content, StandardOpenOption.APPEND);
        return "Appended to: " + path;
    }

    @Tool(name = "edit", description = "Edit file by replacing text")
    public String edit(String path, String oldText, String newText) throws Exception {
        String content = Files.readString(Paths.get(path));
        String updated = content.replace(oldText, newText);
        Files.writeString(Paths.get(path), updated);
        return "Edited: " + path;
    }

    @Tool(name = "edit_line", description = "Edit specific line in file")
    public String editLine(String path, int lineNumber, String newLine) throws Exception {
        List<String> lines = Files.readAllLines(Paths.get(path));
        if (lineNumber >= 0 && lineNumber < lines.size()) {
            lines.set(lineNumber, newLine);
            Files.write(Paths.get(path), lines);
            return "Line " + lineNumber + " edited in: " + path;
        }
        return "Invalid line number";
    }

    @Tool(name = "edit_insert", description = "Insert line at position")
    public String editInsert(String path, int lineNumber, String content) throws Exception {
        List<String> lines = Files.readAllLines(Paths.get(path));
        lines.add(lineNumber, content);
        Files.write(Paths.get(path), lines);
        return "Inserted at line " + lineNumber + " in: " + path;
    }

    @Tool(name = "edit_delete_line", description = "Delete line from file")
    public String editDeleteLine(String path, int lineNumber) throws Exception {
        List<String> lines = Files.readAllLines(Paths.get(path));
        if (lineNumber >= 0 && lineNumber < lines.size()) {
            lines.remove(lineNumber);
            Files.write(Paths.get(path), lines);
            return "Line " + lineNumber + " deleted from: " + path;
        }
        return "Invalid line number";
    }

    @Tool(name = "apply_patch", description = "Apply patch to file")
    public String applyPatch(String filePath, String patch) throws Exception {
        String content = Files.readString(Paths.get(filePath));
        String[] hunks = patch.split("@@");
        for (int i = 1; i < hunks.length; i += 2) {
            String[] lines = hunks[i + 1].split("\n");
            for (String line : lines) {
                if (line.startsWith("-")) content = content.replace(line.substring(1) + "\n", "");
                else if (line.startsWith("+")) content += line.substring(1) + "\n";
            }
        }
        Files.writeString(Paths.get(filePath), content);
        return "Patch applied to: " + filePath;
    }

    @Tool(name = "fs_exists", description = "Check if file or directory exists")
    public boolean fsExists(String path) {
        return Files.exists(Paths.get(path));
    }

    @Tool(name = "fs_is_file", description = "Check if path is a file")
    public boolean fsIsFile(String path) {
        return Files.isRegularFile(Paths.get(path));
    }

    @Tool(name = "fs_is_dir", description = "Check if path is a directory")
    public boolean fsIsDir(String path) {
        return Files.isDirectory(Paths.get(path));
    }

    @Tool(name = "fs_size", description = "Get file size in bytes")
    public long fsSize(String path) throws Exception {
        return Files.size(Paths.get(path));
    }

    @Tool(name = "fs_copy", description = "Copy file or directory")
    public String fsCopy(String source, String target) throws Exception {
        Files.copy(Paths.get(source), Paths.get(target), StandardCopyOption.REPLACE_EXISTING);
        return "Copied: " + source + " -> " + target;
    }

    @Tool(name = "fs_move", description = "Move file or directory")
    public String fsMove(String source, String target) throws Exception {
        Files.move(Paths.get(source), Paths.get(target), StandardCopyOption.REPLACE_EXISTING);
        return "Moved: " + source + " -> " + target;
    }

    @Tool(name = "fs_delete", description = "Delete file or directory")
    public String fsDelete(String path) throws Exception {
        Files.deleteIfExists(Paths.get(path));
        return "Deleted: " + path;
    }

    @Tool(name = "fs_mkdir", description = "Create directory")
    public String fsMkdir(String path) throws Exception {
        Files.createDirectories(Paths.get(path));
        return "Created directory: " + path;
    }

    @Tool(name = "fs_list", description = "List directory contents")
    public String fsList(String path) throws Exception {
        return Files.list(Paths.get(path))
            .map(p -> p.getFileName().toString())
            .collect(Collectors.joining("\n"));
    }

    @Tool(name = "fs_walk", description = "Walk directory tree")
    public String fsWalk(String path) throws Exception {
        return Files.walk(Paths.get(path))
            .map(p -> p.toString())
            .collect(Collectors.joining("\n"));
    }

    @Tool(name = "fs_glob", description = "Find files matching glob pattern")
    public String fsGlob(String directory, String pattern) throws Exception {
        return Files.walk(Paths.get(directory))
            .filter(p -> p.getFileName().toString().matches(pattern.replace("*", ".*")))
            .map(p -> p.toString())
            .collect(Collectors.joining("\n"));
    }
}
