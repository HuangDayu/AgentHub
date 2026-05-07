package com.agenthub.infrastructure.tools.system_tools.base_tools;

import com.agenthub.infrastructure.tools.system_tools.annotations.AgentTools;
import org.springframework.ai.tool.annotation.Tool;

import java.nio.charset.StandardCharsets;

@AgentTools(name = "ConversionTools", description = "单位转换工具，提供进制转换、单位换算(长度、重量、时间等)、编码转换等功能", defaultEnable = false)
public class ConversionTools {

    @Tool(name = "convert_int_to_hex", description = "Convert integer to hexadecimal")
    public String intToHex(int value) {
        return Integer.toHexString(value);
    }

    @Tool(name = "convert_hex_to_int", description = "Convert hexadecimal to integer")
    public int hexToInt(String hex) {
        return Integer.parseInt(hex, 16);
    }

    @Tool(name = "convert_int_to_binary", description = "Convert integer to binary")
    public String intToBinary(int value) {
        return Integer.toBinaryString(value);
    }

    @Tool(name = "convert_binary_to_int", description = "Convert binary to integer")
    public int binaryToInt(String binary) {
        return Integer.parseInt(binary, 2);
    }

    @Tool(name = "convert_int_to_octal", description = "Convert integer to octal")
    public String intToOctal(int value) {
        return Integer.toOctalString(value);
    }

    @Tool(name = "convert_octal_to_int", description = "Convert octal to integer")
    public int octalToInt(String octal) {
        return Integer.parseInt(octal, 8);
    }

    @Tool(name = "convert_celsius_to_fahrenheit", description = "Convert Celsius to Fahrenheit")
    public double celsiusToFahrenheit(double celsius) {
        return celsius * 9 / 5 + 32;
    }

    @Tool(name = "convert_fahrenheit_to_celsius", description = "Convert Fahrenheit to Celsius")
    public double fahrenheitToCelsius(double fahrenheit) {
        return (fahrenheit - 32) * 5 / 9;
    }

    @Tool(name = "convert_km_to_miles", description = "Convert kilometers to miles")
    public double kmToMiles(double km) {
        return km * 0.621371;
    }

    @Tool(name = "convert_miles_to_km", description = "Convert miles to kilometers")
    public double milesToKm(double miles) {
        return miles * 1.60934;
    }

    @Tool(name = "convert_kg_to_lbs", description = "Convert kilograms to pounds")
    public double kgToLbs(double kg) {
        return kg * 2.20462;
    }

    @Tool(name = "convert_lbs_to_kg", description = "Convert pounds to kilograms")
    public double lbsToKg(double lbs) {
        return lbs * 0.453592;
    }

    @Tool(name = "convert_bytes_to_kb", description = "Convert bytes to kilobytes")
    public double bytesToKb(long bytes) {
        return bytes / 1024.0;
    }

    @Tool(name = "convert_bytes_to_mb", description = "Convert bytes to megabytes")
    public double bytesToMb(long bytes) {
        return bytes / (1024.0 * 1024);
    }

    @Tool(name = "convert_bytes_to_gb", description = "Convert bytes to gigabytes")
    public double bytesToGb(long bytes) {
        return bytes / (1024.0 * 1024 * 1024);
    }

    @Tool(name = "convert_string_to_bytes", description = "Convert string to byte array")
    public String stringToBytes(String text) {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(b).append(" ");
        return sb.toString().trim();
    }

    @Tool(name = "convert_bytes_to_string", description = "Convert byte array to string")
    public String bytesToString(String byteStr) {
        String[] parts = byteStr.split("\\s+");
        byte[] bytes = new byte[parts.length];
        for (int i = 0; i < parts.length; i++) bytes[i] = Byte.parseByte(parts[i]);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    @Tool(name = "convert_rad_to_deg", description = "Convert radians to degrees")
    public double radToDeg(double radians) {
        return Math.toDegrees(radians);
    }

    @Tool(name = "convert_deg_to_rad", description = "Convert degrees to radians")
    public double degToRad(double degrees) {
        return Math.toRadians(degrees);
    }

    @Tool(name = "convert_seconds_to_hms", description = "Convert seconds to HH:MM:SS")
    public String secondsToHms(long seconds) {
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    @Tool(name = "convert_hms_to_seconds", description = "Convert HH:MM:SS to seconds")
    public long hmsToSeconds(String hms) {
        String[] parts = hms.split(":");
        return Long.parseLong(parts[0]) * 3600 + Long.parseLong(parts[1]) * 60 + Long.parseLong(parts[2]);
    }
}
