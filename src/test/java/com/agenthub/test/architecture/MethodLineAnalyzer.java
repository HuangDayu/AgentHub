package com.agenthub.test.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 统计 ArchUnit 方法在源文件中的实际代码行数（不含空行和注释）。
 * <p>
 * 供 {@link AgentHubCleanArchitectureTest}（架构测试）和
 * {@link MethodViolationDumper}（违规扫描器）共享，确保两处计数口径一致。
 * <p>
 * <b>算法要点：</b>
 * <ol>
 *     <li>ArchUnit 报告的 {@code declLine} 可能是注解行（如 {@code @SneakyThrows}）或方法体第一行，
 *         因此在 {@code declLine ± 6} 范围内双向扫描方法签名。</li>
 *     <li>通过花括号深度匹配定位方法体结束行。</li>
 *     <li>扫描花括号时先剥离行注释、块注释、字符串和字符字面量，
 *         避免将字符串内的 {@code `${` {@code `}`} 误识别为代码大括号。</li>
 * </ol>
 */
final class MethodLineAnalyzer {

    private static final Pattern SINGLE_LINE = Pattern.compile(
            "^\\s*(public|private|protected|static|final|abstract|synchronized|native|@\\w+)\\b[^=/;]*\\)\\s*(throws\\s+[A-Za-z0-9_.,\\s]+)?\\s*\\{\\s*$");
    private static final Pattern MULTI_LINE_END = Pattern.compile(
            "\\)\\s*(throws\\s+[A-Za-z0-9_.,\\s]+)?\\s*\\{\\s*$");
    private static final Pattern ABSTRACT_SIGNATURE = Pattern.compile(
            "^\\s*(public|private|protected|static|final|abstract|synchronized|native|@\\w+)\\b[^=/]*\\)\\s*(throws\\s+[A-Za-z0-9_.,\\s]+)?\\s*;\\s*$");
    private static final Pattern CONTROL_FLOW = Pattern.compile("^\\s*(try|if|for|while|switch|catch|else)\\b");

    /**
     * 双向扫描方法签名行时的搜索半径，覆盖注解、方法体首行等偏移。
     */
    private static final int SIGNATURE_SEARCH_RADIUS = 6;

    private MethodLineAnalyzer() {
    }

    /**
     * 统计方法在默认源码根目录（{@code <user.dir>/src/main/java}）下的实际代码行数。
     *
     * @return 非空非注释行数；无法定位源文件或方法体时返回 {@code 0}。
     */
    static int countLines(JavaMethod method) {
        Path sourceRoot = Paths.get(System.getProperty("user.dir"), "src/main/java");
        return countLines(method, sourceRoot);
    }

    /**
     * 统计方法在指定源码根目录下的实际代码行数。
     *
     * @return 非空非注释行数；无法定位源文件或方法体时返回 {@code 0}。
     */
    static int countLines(JavaMethod method, Path sourceRoot) {
        JavaClass owner = method.getOwner();
        Path sourceFile = sourceRoot.resolve(owner.getFullName().replace('.', '/') + ".java");
        if (!Files.exists(sourceFile)) {
            return 0;
        }
        int declLine = method.getSourceCodeLocation().getLineNumber();
        if (declLine <= 0) {
            return 0;
        }
        try {
            List<String> lines = Files.readAllLines(sourceFile);
            int anchor = declLine - 1;
            int sigStart = findSignatureLine(lines, anchor, method.getName());
            if (sigStart == anchor) {
                int openingBrace = findMethodOpeningBrace(lines, anchor);
                if (openingBrace > 0) {
                    sigStart = openingBrace;
                }
            }
            int end = findMethodEndLine(lines, sigStart);
            if (end < sigStart) {
                return 0;
            }
            int count = 0;
            for (int k = sigStart; k <= end; k++) {
                if (!stripLiteralsAndComments(lines.get(k)).trim().isEmpty()) {
                    count++;
                }
            }
            return count;
        } catch (IOException ignored) {
            return 0;
        }
    }

    /**
     * 从 {@code anchor} 周围 {@link #SIGNATURE_SEARCH_RADIUS} 行内查找方法签名起始行。
     * <p>
     * ArchUnit 报告的 {@code declLine} 可能是注解行（如 {@code @SneakyThrows}）或方法体第一行，
     * 因此需要双向扫描。
     * <p>
     * 支持三种形式：
     * <ul>
     *     <li>单行签名：{@code ... methodName(params) { ... }}</li>
     *     <li>多行签名：{@code ... methodName(\n  param1,\n  param2\n) { ... }}</li>
     *     <li>抽象方法：{@code ... methodName();}（必须以修饰符或注解开头）</li>
     * </ul>
     * 关键约束：行内不能出现 {@code =}、不能以 {@code ;} 结尾（排除字段赋值和方法调用语句）。
     */
    private static int findSignatureLine(List<String> lines, int anchor, String methodName) {
        int from = Math.max(0, anchor - SIGNATURE_SEARCH_RADIUS);
        int to = Math.min(lines.size() - 1, anchor + SIGNATURE_SEARCH_RADIUS);
        for (int i = from; i <= to; i++) {
            String line = lines.get(i).replaceAll("//.*", "");
            if (SINGLE_LINE.matcher(line).matches() || ABSTRACT_SIGNATURE.matcher(line).matches()) {
                return i;
            }
            if (MULTI_LINE_END.matcher(line).find()) {
                int headerStart = findMultiLineSignatureStart(lines, i, methodName);
                if (headerStart > 0) {
                    return headerStart;
                }
            }
        }
        return anchor;
    }

    /**
     * 多行方法签名：从 {@code )} 开头行向上回溯，
     * 找到包含方法名 + 不含 {@code ;} {@code =} {@code )} 的行。
     * 允许省略修饰符（包级私有方法）。
     */
    private static int findMultiLineSignatureStart(List<String> lines, int multiLineEnd, String methodName) {
        for (int i = multiLineEnd - 1; i >= 0; i--) {
            String line = lines.get(i).replaceAll("//.*", "").trim();
            if (line.isEmpty()) {
                continue;
            }
            if (line.endsWith(")") || line.endsWith(";") || line.contains("=")) {
                return -1;
            }
            if (line.endsWith("{")) {
                return -1;
            }
            String raw = lines.get(i).replaceAll("//.*", "");
            if (raw.contains(methodName + "(")) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 在 {@code findSignatureLine} 失败回退到 {@code anchor} 时使用：
     * 从 {@code anchor} 向前查找方法体的开括号行（含 {@code {} 的行，且不是控制流关键字）。
     * 用于处理包级私有方法（无修饰符、ArchUnit 报告的位置在方法体内）等情况。
     */
    private static int findMethodOpeningBrace(List<String> lines, int anchor) {
        for (int i = anchor; i >= 0; i--) {
            String line = lines.get(i).replaceAll("//.*", "");
            if (CONTROL_FLOW.matcher(line).find()) {
                continue;
            }
            if (line.contains("{")) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 从方法签名行开始向后扫描花括号深度，归零时返回结束行。
     * 该算法先剥离行注释、块注释和字符串/字符字面量，
     * 然后按花括号深度匹配，避开字符串内的 {@code {} {@code }}。
     */
    private static int findMethodEndLine(List<String> lines, int startLine) {
        int depth = 0;
        boolean opened = false;
        for (int i = startLine; i < lines.size(); i++) {
            String line = stripLiteralsAndComments(lines.get(i));
            for (int j = 0; j < line.length(); j++) {
                char c = line.charAt(j);
                if (c == '{') {
                    depth++;
                    opened = true;
                } else if (c == '}') {
                    depth--;
                    if (opened && depth == 0) {
                        return i;
                    }
                }
            }
        }
        return lines.size() - 1;
    }

    /**
     * 剥离行注释、块注释、字符串字面量和字符字面量，避免误识别字符串内的 {@code {} {@code }}。
     * <p>
     * 简单实现：单行内处理，不处理跨行块注释和跨行字符串（罕见且不会让大括号失配）。
     */
    private static String stripLiteralsAndComments(String line) {
        StringBuilder sb = new StringBuilder(line);
        int i = 0;
        while (i < sb.length()) {
            char c = sb.charAt(i);
            if (c == '/' && i + 1 < sb.length() && sb.charAt(i + 1) == '/') {
                sb.delete(i, sb.length());
                break;
            } else if (c == '/' && i + 1 < sb.length() && sb.charAt(i + 1) == '*') {
                int end = sb.indexOf("*/", i + 2);
                if (end >= 0) {
                    sb.delete(i, end + 2);
                } else {
                    sb.delete(i, sb.length());
                    break;
                }
            } else if (c == '"') {
                i = skipQuoted(sb, i, '"');
            } else if (c == '\'') {
                i = skipQuoted(sb, i, '\'');
            } else {
                i++;
            }
        }
        return sb.toString();
    }

    /**
     * 跳过从 {@code start} 开始到配对引号结束的字符串/字符字面量，并返回结束索引。
     */
    private static int skipQuoted(StringBuilder sb, int start, char quote) {
        int end = start + 1;
        while (end < sb.length() && sb.charAt(end) != quote) {
            if (sb.charAt(end) == '\\' && end + 1 < sb.length()) {
                end += 2;
            } else {
                end++;
            }
        }
        if (end < sb.length()) {
            sb.delete(start, end + 1);
        } else {
            sb.delete(start, sb.length());
        }
        return start;
    }
}
