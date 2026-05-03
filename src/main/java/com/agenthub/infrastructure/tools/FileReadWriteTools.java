package com.agenthub.infrastructure.tools;

import com.agenthub.infrastructure.tools.annotations.AgentTool;
import com.agenthub.infrastructure.tools.annotations.ToolParameter;
import org.springframework.stereotype.Component;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component
public class FileReadWriteTools {
    
    @AgentTool(name = "file_read", description = "Read file content", tags = {"file", "io"})
    public String readFile(@ToolParameter(name = "path", description = "File path") String path) throws Exception {
        return Files.readString(Paths.get(path));
    }
    
    @AgentTool(name = "file_write", description = "Write content to file", tags = {"file", "io"})
    public String writeFile(@ToolParameter(name = "path") String path, 
                           @ToolParameter(name = "content") String content) throws Exception {
        Files.writeString(Paths.get(path), content);
        return "Written to " + path;
    }
    
    @AgentTool(name = "file_list", description = "List directory contents", tags = {"file", "io"})
    public String listDirectory(@ToolParameter(name = "path") String path) throws Exception {
        StringBuilder result = new StringBuilder();
        Files.list(Paths.get(path)).forEach(p -> result.append(p.toString()).append("\n"));
        return result.toString();
    }
    
    @AgentTool(name = "file_exists", description = "Check if file exists", tags = {"file", "io"})
    public boolean fileExists(@ToolParameter(name = "path") String path) {
        return Files.exists(Paths.get(path));
    }
}
