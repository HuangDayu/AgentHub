package com.agenthub.infrastructure.tools.function_tools.base_tools;

import com.agenthub.infrastructure.tools.function_tools.annotations.AgentTools;
import org.springframework.ai.tool.annotation.Tool;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@AgentTools(name = "TextProcessingTools", description = "文本处理工具，提供文本长度统计、大小写转换、分割合并、去重排序、填充对齐等文本处理功能", defaultEnable = false)
public class TextProcessingTools {

    @Tool(name = "text_length", description = "Get text length")
    public int getLength(String text) {
        return text.length();
    }

    @Tool(name = "text_word_count", description = "Count words in text")
    public int wordCount(String text) {
        return text.split("\\s+").length;
    }

    @Tool(name = "text_line_count", description = "Count lines in text")
    public int lineCount(String text) {
        return text.split("\n").length;
    }

    @Tool(name = "text_char_count", description = "Count characters excluding spaces")
    public int charCount(String text) {
        return text.replace(" ", "").length();
    }

    @Tool(name = "text_upper", description = "Convert to uppercase")
    public String toUpper(String text) {
        return text.toUpperCase();
    }

    @Tool(name = "text_lower", description = "Convert to lowercase")
    public String toLower(String text) {
        return text.toLowerCase();
    }

    @Tool(name = "text_capitalize", description = "Capitalize first letter")
    public String capitalize(String text) {
        if (text.isEmpty()) return text;
        return Character.toUpperCase(text.charAt(0)) + text.substring(1).toLowerCase();
    }

    @Tool(name = "text_reverse", description = "Reverse text")
    public String reverse(String text) {
        return new StringBuilder(text).reverse().toString();
    }

    @Tool(name = "text_trim", description = "Trim whitespace")
    public String trim(String text) {
        return text.trim();
    }

    @Tool(name = "text_replace", description = "Replace all occurrences")
    public String replace(String text, String from, String to) {
        return text.replace(from, to);
    }

    @Tool(name = "text_replace_regex", description = "Replace using regex")
    public String replaceRegex(String text, String regex, String replacement) {
        return text.replaceAll(regex, replacement);
    }

    @Tool(name = "text_split", description = "Split text by delimiter")
    public String split(String text, String delimiter) {
        return String.join("\n", text.split(delimiter));
    }

    @Tool(name = "text_join", description = "Join lines with delimiter")
    public String join(String text, String delimiter) {
        List<String> lines = Arrays.asList(text.split("\n"));
        return String.join(delimiter, lines);
    }

    @Tool(name = "text_substring", description = "Extract substring")
    public String substring(String text, int start, int end) {
        return text.substring(start, end);
    }

    @Tool(name = "text_contains", description = "Check if text contains substring")
    public boolean contains(String text, String substring) {
        return text.contains(substring);
    }

    @Tool(name = "text_starts_with", description = "Check if text starts with prefix")
    public boolean startsWith(String text, String prefix) {
        return text.startsWith(prefix);
    }

    @Tool(name = "text_ends_with", description = "Check if text ends with suffix")
    public boolean endsWith(String text, String suffix) {
        return text.endsWith(suffix);
    }

    @Tool(name = "text_index_of", description = "Find index of substring")
    public int indexOf(String text, String substring) {
        return text.indexOf(substring);
    }

    @Tool(name = "text_last_index_of", description = "Find last index of substring")
    public int lastIndexOf(String text, String substring) {
        return text.lastIndexOf(substring);
    }

    @Tool(name = "text_repeat", description = "Repeat text n times")
    public String repeat(String text, int times) {
        return text.repeat(times);
    }

    @Tool(name = "text_remove_duplicates", description = "Remove duplicate lines")
    public String removeDuplicates(String text) {
        return Arrays.stream(text.split("\n"))
            .distinct()
            .collect(Collectors.joining("\n"));
    }

    @Tool(name = "text_sort_lines", description = "Sort lines alphabetically")
    public String sortLines(String text) {
        return Arrays.stream(text.split("\n"))
            .sorted()
            .collect(Collectors.joining("\n"));
    }

    @Tool(name = "text_unique_words", description = "Get unique words")
    public String uniqueWords(String text) {
        return Arrays.stream(text.toLowerCase().split("\\W+"))
            .distinct()
            .sorted()
            .collect(Collectors.joining(" "));
    }

    @Tool(name = "text_pad_left", description = "Pad text on left")
    public String padLeft(String text, int length, char padChar) {
        return String.format("%" + length + "s", text).replace(' ', padChar);
    }

    @Tool(name = "text_pad_right", description = "Pad text on right")
    public String padRight(String text, int length, char padChar) {
        return String.format("%-" + length + "s", text).replace(' ', padChar);
    }
}
