package com.agenthub.infrastructure.tools.system_tools.base_tools;

import com.agenthub.infrastructure.tools.system_tools.annotations.AgentTools;
import org.springframework.ai.tool.annotation.Tool;

import java.util.regex.Pattern;

@AgentTools(name = "ValidationTools", description = "数据验证工具，提供邮箱、电话、URL、IP地址、信用卡号、密码强度等数据格式验证功能", defaultEnable = false)
public class ValidationTools {

    @Tool(name = "validate_email", description = "Validate email format")
    public boolean validateEmail(String email) {
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return Pattern.matches(regex, email);
    }

    @Tool(name = "validate_phone", description = "Validate phone number format")
    public boolean validatePhone(String phone) {
        String regex = "^\\+?[0-9]{10,15}$";
        return Pattern.matches(regex, phone.replaceAll("[\\s\\-\\(\\)]", ""));
    }

    @Tool(name = "validate_url", description = "Validate URL format")
    public boolean validateUrl(String url) {
        String regex = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$";
        return Pattern.matches(regex, url);
    }

    @Tool(name = "validate_ipv4", description = "Validate IPv4 address")
    public boolean validateIpv4(String ip) {
        String regex = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
        return Pattern.matches(regex, ip);
    }

    @Tool(name = "validate_ipv6", description = "Validate IPv6 address")
    public boolean validateIpv6(String ip) {
        String regex = "^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$";
        return Pattern.matches(regex, ip);
    }

    @Tool(name = "validate_hex_color", description = "Validate hex color code")
    public boolean validateHexColor(String color) {
        String regex = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$";
        return Pattern.matches(regex, color);
    }

    @Tool(name = "validate_date_iso", description = "Validate ISO date format YYYY-MM-DD")
    public boolean validateDateIso(String date) {
        String regex = "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])$";
        return Pattern.matches(regex, date);
    }

    @Tool(name = "validate_time_24h", description = "Validate 24-hour time format")
    public boolean validateTime24h(String time) {
        String regex = "^([01]?[0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?$";
        return Pattern.matches(regex, time);
    }

    @Tool(name = "validate_uuid", description = "Validate UUID format")
    public boolean validateUuid(String uuid) {
        String regex = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
        return Pattern.matches(regex, uuid);
    }

    @Tool(name = "validate_credit_card", description = "Validate credit card number")
    public boolean validateCreditCard(String number) {
        String digits = number.replaceAll("[^0-9]", "");
        if (digits.length() < 13 || digits.length() > 19) return false;
        return luhnCheck(digits);
    }

    private boolean luhnCheck(String number) {
        int sum = 0;
        boolean alternate = false;
        for (int i = number.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(number.charAt(i));
            if (alternate) {
                digit *= 2;
                if (digit > 9) digit -= 9;
            }
            sum += digit;
            alternate = !alternate;
        }
        return sum % 10 == 0;
    }

    @Tool(name = "validate_json", description = "Check if string is valid JSON")
    public boolean validateJson(String text) {
        text = text.trim();
        if (text.startsWith("{") && text.endsWith("}")) return true;
        if (text.startsWith("[") && text.endsWith("]")) return true;
        return false;
    }

    @Tool(name = "validate_alpha", description = "Check if string is alphabetic")
    public boolean validateAlpha(String text) {
        return text.matches("^[a-zA-Z]+$");
    }

    @Tool(name = "validate_alphanumeric", description = "Check if string is alphanumeric")
    public boolean validateAlphanumeric(String text) {
        return text.matches("^[a-zA-Z0-9]+$");
    }

    @Tool(name = "validate_numeric", description = "Check if string is numeric")
    public boolean validateNumeric(String text) {
        return text.matches("^-?\\d+(\\.\\d+)?$");
    }

    @Tool(name = "validate_integer", description = "Check if string is integer")
    public boolean validateInteger(String text) {
        return text.matches("^-?\\d+$");
    }

    @Tool(name = "validate_positive_integer", description = "Check if string is positive integer")
    public boolean validatePositiveInteger(String text) {
        return text.matches("^[1-9]\\d*$");
    }

    @Tool(name = "validate_username", description = "Validate username format")
    public boolean validateUsername(String username) {
        return username.matches("^[a-zA-Z][a-zA-Z0-9_]{2,15}$");
    }

    @Tool(name = "validate_password_strength", description = "Check password strength")
    public String validatePasswordStrength(String password) {
        int score = 0;
        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++;
        if (password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*[a-z].*")) score++;
        if (password.matches(".*[0-9].*")) score++;
        if (password.matches(".*[!@#$%^&*].*")) score++;
        if (score < 3) return "Weak";
        if (score < 5) return "Medium";
        return "Strong";
    }

    @Tool(name = "validate_regex", description = "Match string against regex pattern")
    public boolean validateRegex(String text, String pattern) {
        return Pattern.matches(pattern, text);
    }
}
