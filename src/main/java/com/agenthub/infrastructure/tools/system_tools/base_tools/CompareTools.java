package com.agenthub.infrastructure.tools.system_tools.base_tools;

import com.agenthub.infrastructure.tools.system_tools.annotations.AgentTools;
import org.springframework.ai.tool.annotation.Tool;

@AgentTools(name = "CompareTools", description = "比较工具，提供字符串比较、数值比较、相似度计算、差异查找等比较功能", defaultEnable = false)
public class CompareTools {

    @Tool(name = "compare_strings", description = "Compare two strings lexicographically")
    public int compareStrings(String str1, String str2) {
        return str1.compareTo(str2);
    }

    @Tool(name = "compare_strings_ignore_case", description = "Compare strings ignoring case")
    public int compareStringsIgnoreCase(String str1, String str2) {
        return str1.compareToIgnoreCase(str2);
    }

    @Tool(name = "compare_numbers", description = "Compare two numbers")
    public int compareNumbers(double num1, double num2) {
        return Double.compare(num1, num2);
    }

    @Tool(name = "compare_dates", description = "Compare two ISO dates")
    public int compareDates(String date1, String date2) {
        return date1.compareTo(date2);
    }

    @Tool(name = "compare_versions", description = "Compare version strings")
    public int compareVersions(String v1, String v2) {
        String[] parts1 = v1.split("\\.");
        String[] parts2 = v2.split("\\.");
        int maxLen = Math.max(parts1.length, parts2.length);
        for (int i = 0; i < maxLen; i++) {
            int num1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
            int num2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;
            if (num1 != num2) return Integer.compare(num1, num2);
        }
        return 0;
    }

    @Tool(name = "compare_equals", description = "Check if two values are equal")
    public boolean equals(String value1, String value2) {
        return value1.equals(value2);
    }

    @Tool(name = "compare_equals_ignore_case", description = "Check equality ignoring case")
    public boolean equalsIgnoreCase(String value1, String value2) {
        return value1.equalsIgnoreCase(value2);
    }

    @Tool(name = "compare_contains", description = "Check if first contains second")
    public boolean contains(String str1, String str2) {
        return str1.contains(str2);
    }

    @Tool(name = "compare_starts_with", description = "Check if first starts with second")
    public boolean startsWith(String str1, String str2) {
        return str1.startsWith(str2);
    }

    @Tool(name = "compare_ends_with", description = "Check if first ends with second")
    public boolean endsWith(String str1, String str2) {
        return str1.endsWith(str2);
    }

    @Tool(name = "compare_similarity", description = "Calculate string similarity (0-100)")
    public int similarity(String str1, String str2) {
        int maxLen = Math.max(str1.length(), str2.length());
        if (maxLen == 0) return 100;
        int distance = levenshteinDistance(str1, str2);
        return (int) ((1 - (double) distance / maxLen) * 100);
    }

    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= s2.length(); j++) dp[0][j] = j;
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }
        return dp[s1.length()][s2.length()];
    }

    @Tool(name = "compare_diff_lines", description = "Find different lines between two texts")
    public String diffLines(String text1, String text2) {
        String[] lines1 = text1.split("\n");
        String[] lines2 = text2.split("\n");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.max(lines1.length, lines2.length); i++) {
            String l1 = i < lines1.length ? lines1[i] : "";
            String l2 = i < lines2.length ? lines2[i] : "";
            if (!l1.equals(l2)) sb.append("Line ").append(i + 1).append(": [").append(l1).append("] vs [").append(l2).append("]\n");
        }
        return sb.toString();
    }

    @Tool(name = "compare_in_range", description = "Check if number is in range")
    public boolean inRange(double value, double min, double max) {
        return value >= min && value <= max;
    }

    @Tool(name = "compare_greater", description = "Check if first > second")
    public boolean greater(double num1, double num2) {
        return num1 > num2;
    }

    @Tool(name = "compare_less", description = "Check if first < second")
    public boolean less(double num1, double num2) {
        return num1 < num2;
    }

    @Tool(name = "compare_max", description = "Get maximum value")
    public double max(double num1, double num2) {
        return Math.max(num1, num2);
    }

    @Tool(name = "compare_min", description = "Get minimum value")
    public double min(double num1, double num2) {
        return Math.min(num1, num2);
    }
}
