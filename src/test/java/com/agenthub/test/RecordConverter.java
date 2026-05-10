package com.agenthub.test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RecordConverter {
    public static void main(String[] args) throws Exception {
        Path root = Paths.get(System.getProperty("user.dir")+"/src/main/java/com/agenthub");
        Files.walk(root)
                .filter(p -> p.toString().endsWith(".java"))
                .forEach(RecordConverter::convertFile);
    }

    static void convertFile(Path file) {
        try {
            String content = Files.readString(file);
            if (!content.contains("public record ")) return;

            // 跳过复杂record
            if (content.contains("public " + getClassName(content) + " {") ||
                    content.indexOf("public record ") != content.lastIndexOf("public record ")) {
                System.out.println("跳过复杂record: " + file.getFileName());
                return;
            }

            String converted = convertRecord(content);
            Files.writeString(file, converted);
            System.out.println("已转换: " + file.getFileName());
        } catch (Exception e) {
            System.err.println("错误: " + file + " - " + e.getMessage());
        }
    }

    static String getClassName(String content) {
        Matcher m = Pattern.compile("public record (\\w+)").matcher(content);
        return m.find() ? m.group(1) : "";
    }

    static String convertRecord(String content) {
        // 提取package
        Matcher pm = Pattern.compile("package ([\\w.]+);").matcher(content);
        String pkg = pm.find() ? pm.group(1) : "";

        // 提取imports
        List<String> imports = new ArrayList<>();
        Matcher im = Pattern.compile("import ([\\w.]+);").matcher(content);
        while (im.find()) imports.add(im.group(1));

        // 提取record定义
        Matcher rm = Pattern.compile("public record (\\w+)(?:<([^>]+)>)?\\s*\\(([^)]*)\\)\\s*\\{\\s*\\}", Pattern.DOTALL).matcher(content);
        if (!rm.find()) return content;

        String className = rm.group(1);
        String generic = rm.group(2);
        String fieldsStr = rm.group(3);

        // 解析字段
        List<String[]> fields = parseFields(fieldsStr);

        // 生成class
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(pkg).append(";\n\n");
        imports.forEach(i -> sb.append("import ").append(i).append(";\n"));
        if (!imports.isEmpty()) sb.append("\n");

        sb.append("import lombok.Data;\n");
        sb.append("import lombok.NoArgsConstructor;\n");
        sb.append("import lombok.AllArgsConstructor;\n\n");

        sb.append("@Data\n");
        sb.append("@NoArgsConstructor\n");
        sb.append("@AllArgsConstructor\n");
        sb.append("public class ").append(className);
        if (generic != null) sb.append("<").append(generic).append(">");
        sb.append(" {\n");

        for (String[] f : fields) {
            sb.append("    private ").append(f[0]).append(" ").append(f[1]).append(";\n");
        }

        sb.append("}\n");
        return sb.toString();
    }

    static List<String[]> parseFields(String fieldsStr) {
        List<String[]> fields = new ArrayList<>();
        if (fieldsStr.isBlank()) return fields;

        String normalized = fieldsStr.replace("\n", " ").replace("\r", " ");
        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int depth = 0;

        for (char c : normalized.toCharArray()) {
            if (c == '<') depth++;
            else if (c == '>') depth--;
            else if (c == ',' && depth == 0) {
                if (current.length() > 0) parts.add(current.toString().trim());
                current = new StringBuilder();
                continue;
            }
            current.append(c);
        }
        if (current.length() > 0) parts.add(current.toString().trim());

        for (String part : parts) {
            String[] tokens = part.split("\\s+");
            if (tokens.length >= 2) {
                String type = String.join(" ", Arrays.copyOf(tokens, tokens.length - 1));
                String name = tokens[tokens.length - 1];
                fields.add(new String[]{type, name});
            }
        }

        return fields;
    }
}