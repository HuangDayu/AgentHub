package com.agenthub.infrastructure.tools.function_tools.base_tools;

import com.agenthub.infrastructure.tools.function_tools.annotations.AgentTools;
import org.springframework.ai.tool.annotation.Tool;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

@AgentTools(name = "FormatTools", description = "格式化工具，提供数字、货币、百分比、日期、电话号码、表格等格式化功能", defaultEnable = false)
public class FormatTools {

    @Tool(name = "format_number", description = "Format number with commas")
    public String formatNumber(double number) {
        return NumberFormat.getNumberInstance().format(number);
    }

    @Tool(name = "format_currency", description = "Format as currency")
    public String formatCurrency(double amount) {
        return NumberFormat.getCurrencyInstance().format(amount);
    }

    @Tool(name = "format_currency_locale", description = "Format as currency with locale")
    public String formatCurrencyLocale(double amount, String localeTag) {
        Locale locale = Locale.forLanguageTag(localeTag);
        return NumberFormat.getCurrencyInstance(locale).format(amount);
    }

    @Tool(name = "format_percent", description = "Format as percentage")
    public String formatPercent(double value) {
        return NumberFormat.getPercentInstance().format(value);
    }

    @Tool(name = "format_decimal", description = "Format with decimal places")
    public String formatDecimal(double number, String pattern) {
        return new DecimalFormat(pattern).format(number);
    }

    @Tool(name = "format_scientific", description = "Format in scientific notation")
    public String formatScientific(double number) {
        return new DecimalFormat("0.###E0").format(number);
    }

    @Tool(name = "format_bytes", description = "Format bytes to human readable")
    public String formatBytes(long bytes) {
        String[] units = {"B", "KB", "MB", "GB", "TB", "PB"};
        int unitIndex = 0;
        double size = bytes;
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        return String.format("%.2f %s", size, units[unitIndex]);
    }

    @Tool(name = "format_duration", description = "Format milliseconds to duration")
    public String formatDuration(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        if (days > 0) return days + "d " + (hours % 24) + "h";
        if (hours > 0) return hours + "h " + (minutes % 60) + "m";
        if (minutes > 0) return minutes + "m " + (seconds % 60) + "s";
        return seconds + "s";
    }

    @Tool(name = "format_phone", description = "Format phone number")
    public String formatPhone(String phone) {
        String digits = phone.replaceAll("[^0-9]", "");
        if (digits.length() == 10) {
            return String.format("(%s) %s-%s", digits.substring(0, 3), digits.substring(3, 6), digits.substring(6));
        }
        return phone;
    }

    @Tool(name = "format_credit_card", description = "Format credit card number")
    public String formatCreditCard(String number) {
        String digits = number.replaceAll("[^0-9]", "");
        return String.format("%s %s %s %s", digits.substring(0, 4), digits.substring(4, 8), 
            digits.substring(8, 12), digits.substring(12, 16));
    }

    @Tool(name = "format_ssn", description = "Format SSN")
    public String formatSsn(String ssn) {
        String digits = ssn.replaceAll("[^0-9]", "");
        return String.format("%s-%s-%s", digits.substring(0, 3), digits.substring(3, 5), digits.substring(5, 9));
    }

    @Tool(name = "format_table", description = "Format data as simple table")
    public String formatTable(String data) {
        String[] rows = data.split("\n");
        StringBuilder sb = new StringBuilder();
        for (String row : rows) {
            String[] cols = row.split(",");
            sb.append(String.join(" | ", cols)).append("\n");
        }
        return sb.toString();
    }

    @Tool(name = "format_indent", description = "Indent text with spaces")
    public String formatIndent(String text, int spaces) {
        String indent = " ".repeat(spaces);
        return text.lines().map(line -> indent + line).reduce((a, b) -> a + "\n" + b).orElse("");
    }

    @Tool(name = "format_center", description = "Center text in width")
    public String formatCenter(String text, int width) {
        int padding = (width - text.length()) / 2;
        return " ".repeat(Math.max(0, padding)) + text + " ".repeat(Math.max(0, width - text.length() - padding));
    }

    @Tool(name = "format_right_align", description = "Right align text in width")
    public String formatRightAlign(String text, int width) {
        return String.format("%" + width + "s", text);
    }

    @Tool(name = "format_left_align", description = "Left align text in width")
    public String formatLeftAlign(String text, int width) {
        return String.format("%-" + width + "s", text);
    }

    @Tool(name = "format_truncate", description = "Truncate text with ellipsis")
    public String formatTruncate(String text, int maxLength) {
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
}
