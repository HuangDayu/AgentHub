package com.agenthub.infrastructure.tools.function_tools.base_tools;

import com.agenthub.infrastructure.tools.function_tools.annotations.AgentTools;
import org.springframework.ai.tool.annotation.Tool;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

@AgentTools(name = "SearchTools", description = "文件搜索工具，提供文件内容搜索、文件名搜索、目录遍历、重复文件查找等文件搜索功能", defaultEnable = false)
public class SearchTools {

    @Tool(name = "search_in_file", description = "Search text in file")
    public String inFile(String filePath, String searchText) throws Exception {
        String content = Files.readString(Path.of(filePath));
        StringBuilder sb = new StringBuilder();
        String[] lines = content.split("\n");
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains(searchText)) {
                sb.append("Line ").append(i + 1).append(": ").append(lines[i]).append("\n");
            }
        }
        return sb.toString();
    }

    @Tool(name = "search_files_by_name", description = "Search files by name pattern")
    public String filesByName(String directory, String pattern) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (Stream<Path> paths = Files.walk(Path.of(directory))) {
            paths.filter(Files::isRegularFile)
                 .filter(p -> p.getFileName().toString().contains(pattern))
                 .forEach(p -> sb.append(p).append("\n"));
        }
        return sb.toString();
    }

    @Tool(name = "search_files_by_extension", description = "Search files by extension")
    public String filesByExtension(String directory, String extension) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (Stream<Path> paths = Files.walk(Path.of(directory))) {
            paths.filter(Files::isRegularFile)
                 .filter(p -> p.toString().endsWith(extension))
                 .forEach(p -> sb.append(p).append("\n"));
        }
        return sb.toString();
    }

    @Tool(name = "search_directories", description = "Search directories by name")
    public String directories(String directory, String namePattern) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (Stream<Path> paths = Files.walk(Path.of(directory))) {
            paths.filter(Files::isDirectory)
                 .filter(p -> p.getFileName().toString().contains(namePattern))
                 .forEach(p -> sb.append(p).append("\n"));
        }
        return sb.toString();
    }

    @Tool(name = "search_largest_files", description = "Find largest files in directory")
    public String largestFiles(String directory, int count) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (Stream<Path> paths = Files.walk(Path.of(directory))) {
            paths.filter(Files::isRegularFile)
                 .sorted((a, b) -> {
                     try {
                         return Long.compare(Files.size(b), Files.size(a));
                     } catch (Exception e) { return 0; }
                 })
                 .limit(count)
                 .forEach(p -> {
                     try {
                         sb.append(p).append(" (").append(Files.size(p)).append(" bytes)\n");
                     } catch (Exception e) {}
                 });
        }
        return sb.toString();
    }

    @Tool(name = "search_newest_files", description = "Find newest files in directory")
    public String newestFiles(String directory, int count) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (Stream<Path> paths = Files.walk(Path.of(directory))) {
            paths.filter(Files::isRegularFile)
                 .sorted((a, b) -> {
                     try {
                         return Files.getLastModifiedTime(b).compareTo(Files.getLastModifiedTime(a));
                     } catch (Exception e) { return 0; }
                 })
                 .limit(count)
                 .forEach(p -> sb.append(p).append("\n"));
        }
        return sb.toString();
    }

    @Tool(name = "search_empty_files", description = "Find empty files")
    public String emptyFiles(String directory) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (Stream<Path> paths = Files.walk(Path.of(directory))) {
            paths.filter(Files::isRegularFile)
                 .filter(p -> { try { return Files.size(p) == 0; } catch (Exception e) { return false; } })
                 .forEach(p -> sb.append(p).append("\n"));
        }
        return sb.toString();
    }

    @Tool(name = "search_empty_directories", description = "Find empty directories")
    public String emptyDirectories(String directory) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (Stream<Path> paths = Files.walk(Path.of(directory))) {
            paths.filter(Files::isDirectory)
                 .filter(p -> {
                     try (Stream<Path> s = Files.list(p)) {
                         return s.findFirst().isEmpty();
                     } catch (Exception e) { return false; }
                 })
                 .forEach(p -> sb.append(p).append("\n"));
        }
        return sb.toString();
    }

    @Tool(name = "search_duplicate_names", description = "Find files with same name")
    public String duplicateNames(String directory) throws Exception {
        java.util.Map<String, java.util.List<Path>> nameMap = new java.util.HashMap<>();
        try (Stream<Path> paths = Files.walk(Path.of(directory))) {
            paths.filter(Files::isRegularFile)
                 .forEach(p -> nameMap.computeIfAbsent(p.getFileName().toString(), k -> new java.util.ArrayList<>()).add(p));
        }
        StringBuilder sb = new StringBuilder();
        nameMap.values().stream()
               .filter(list -> list.size() > 1)
               .forEach(list -> { sb.append("Duplicate: ").append(list.get(0).getFileName()).append("\n");
                                  list.forEach(p -> sb.append("  ").append(p).append("\n")); });
        return sb.toString();
    }

    @Tool(name = "search_by_content", description = "Search content in multiple files")
    public String byContent(String directory, String searchText, String extension) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (Stream<Path> paths = Files.walk(Path.of(directory))) {
            paths.filter(Files::isRegularFile)
                 .filter(p -> p.toString().endsWith(extension))
                 .forEach(p -> {
                     try {
                         String content = Files.readString(p);
                         if (content.contains(searchText)) {
                             sb.append(p).append("\n");
                         }
                     } catch (Exception e) {}
                 });
        }
        return sb.toString();
    }
}
