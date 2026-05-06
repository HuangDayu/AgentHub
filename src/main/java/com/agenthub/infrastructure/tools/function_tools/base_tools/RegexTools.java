package com.agenthub.infrastructure.tools.function_tools.base_tools;

import com.agenthub.infrastructure.tools.function_tools.annotations.AgentTools;
import org.springframework.ai.tool.annotation.Tool;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@AgentTools(name = "RegexTools", description = "正则表达式工具，提供正则匹配、查找、替换、分组提取等正则表达式操作功能", defaultEnable = false)
public class RegexTools {

    @Tool(name = "regex_match", description = "Check if text matches pattern")
    public boolean match(String text, String pattern) {
        return Pattern.matches(pattern, text);
    }

    @Tool(name = "regex_find", description = "Find first match")
    public String find(String text, String pattern) {
        Matcher m = Pattern.compile(pattern).matcher(text);
        return m.find() ? m.group() : "";
    }

    @Tool(name = "regex_find_all", description = "Find all matches")
    public String findAll(String text, String pattern) {
        Matcher m = Pattern.compile(pattern).matcher(text);
        StringBuilder sb = new StringBuilder();
        while (m.find()) sb.append(m.group()).append("\n");
        return sb.toString();
    }

    @Tool(name = "regex_replace", description = "Replace all matches")
    public String replace(String text, String pattern, String replacement) {
        return text.replaceAll(pattern, replacement);
    }

    @Tool(name = "regex_replace_first", description = "Replace first match")
    public String replaceFirst(String text, String pattern, String replacement) {
        return text.replaceFirst(pattern, replacement);
    }

    @Tool(name = "regex_split", description = "Split text by pattern")
    public String split(String text, String pattern) {
        return String.join("\n", text.split(pattern));
    }

    @Tool(name = "regex_groups", description = "Extract all groups from match")
    public String groups(String text, String pattern) {
        Matcher m = Pattern.compile(pattern).matcher(text);
        StringBuilder sb = new StringBuilder();
        if (m.find()) {
            for (int i = 1; i <= m.groupCount(); i++) {
                sb.append("Group ").append(i).append(": ").append(m.group(i)).append("\n");
            }
        }
        return sb.toString();
    }

    @Tool(name = "regex_count", description = "Count number of matches")
    public int count(String text, String pattern) {
        Matcher m = Pattern.compile(pattern).matcher(text);
        int count = 0;
        while (m.find()) count++;
        return count;
    }

    @Tool(name = "regex_start_index", description = "Get start index of first match")
    public int startIndex(String text, String pattern) {
        Matcher m = Pattern.compile(pattern).matcher(text);
        return m.find() ? m.start() : -1;
    }

    @Tool(name = "regex_end_index", description = "Get end index of first match")
    public int endIndex(String text, String pattern) {
        Matcher m = Pattern.compile(pattern).matcher(text);
        return m.find() ? m.end() : -1;
    }

    @Tool(name = "regex_positions", description = "Get all match positions")
    public String positions(String text, String pattern) {
        Matcher m = Pattern.compile(pattern).matcher(text);
        StringBuilder sb = new StringBuilder();
        while (m.find()) sb.append(m.start()).append("-").append(m.end()).append("\n");
        return sb.toString();
    }

    @Tool(name = "regex_quote", description = "Quote special regex characters")
    public String quote(String text) {
        return Pattern.quote(text);
    }

    @Tool(name = "regex_validate", description = "Validate regex pattern syntax")
    public boolean validate(String pattern) {
        try {
            Pattern.compile(pattern);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Tool(name = "regex_flags_case_insensitive", description = "Match case insensitive")
    public boolean matchCaseInsensitive(String text, String pattern) {
        return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(text).matches();
    }

    @Tool(name = "regex_flags_multiline", description = "Match with multiline mode")
    public boolean matchMultiline(String text, String pattern) {
        return Pattern.compile(pattern, Pattern.MULTILINE).matcher(text).matches();
    }

    @Tool(name = "regex_flags_dotall", description = "Match with dotall mode")
    public boolean matchDotall(String text, String pattern) {
        return Pattern.compile(pattern, Pattern.DOTALL).matcher(text).matches();
    }

    @Tool(name = "regex_lookahead", description = "Find matches with lookahead")
    public String lookahead(String text, String pattern) {
        String fullPattern = "(?=(" + pattern + "))";
        Matcher m = Pattern.compile(fullPattern).matcher(text);
        StringBuilder sb = new StringBuilder();
        while (m.find()) sb.append(m.group(1)).append("\n");
        return sb.toString();
    }
}
