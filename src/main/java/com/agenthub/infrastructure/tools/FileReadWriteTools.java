package com.agenthub.infrastructure.tools;

import com.agenthub.infrastructure.tools.annotations.AgentTools;
import org.springframework.ai.tool.annotation.Tool;

import java.nio.file.Files;
import java.nio.file.Paths;

@AgentTools
public class FileReadWriteTools {

    @Tool(name = "file_read", description = "Read file content")
    public String readFile(String path) throws Exception {
        return Files.readString(Paths.get(path));
    }

    @Tool(name = "file_write", description = "Write content to file")
    public String writeFile(String path,
                            String content) throws Exception {
        Files.writeString(Paths.get(path), content);
        return "Written to " + path;
    }

    @Tool(name = "file_list", description = "List directory contents")
    public String listDirectory(String path) throws Exception {
        StringBuilder result = new StringBuilder();
        Files.list(Paths.get(path)).forEach(p -> result.append(p.toString()).append("\n"));
        return result.toString();
    }

    @Tool(name = "file_exists", description = "Check if file exists")
    public boolean fileExists(String path) {
        return Files.exists(Paths.get(path));
    }
}
