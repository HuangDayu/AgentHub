package com.agenthub.infrastructure.tools.function_tools.base_tools;

import com.agenthub.infrastructure.tools.function_tools.annotations.AgentTools;
import org.springframework.ai.tool.annotation.Tool;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@AgentTools(name = "EncodingTools", description = "编码解码工具，提供Base64、Hex、Binary、Octal、ROT13、ASCII等编码解码功能", defaultEnable = false)
public class EncodingTools {

    @Tool(name = "encode_base64", description = "Encode string to Base64")
    public String encodeBase64(String text) {
        return Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }

    @Tool(name = "decode_base64", description = "Decode Base64 to string")
    public String decodeBase64(String encoded) {
        byte[] decoded = Base64.getDecoder().decode(encoded);
        return new String(decoded, StandardCharsets.UTF_8);
    }

    @Tool(name = "encode_base64_url", description = "Encode to URL-safe Base64")
    public String encodeBase64Url(String text) {
        return Base64.getUrlEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }

    @Tool(name = "decode_base64_url", description = "Decode URL-safe Base64")
    public String decodeBase64Url(String encoded) {
        byte[] decoded = Base64.getUrlDecoder().decode(encoded);
        return new String(decoded, StandardCharsets.UTF_8);
    }

    @Tool(name = "encode_hex", description = "Encode string to hex")
    public String encodeHex(String text) {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    @Tool(name = "decode_hex", description = "Decode hex to string")
    public String decodeHex(String hex) {
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    @Tool(name = "encode_binary", description = "Encode string to binary")
    public String encodeBinary(String text) {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
        return sb.toString();
    }

    @Tool(name = "decode_binary", description = "Decode binary to string")
    public String decodeBinary(String binary) {
        byte[] bytes = new byte[binary.length() / 8];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(binary.substring(8 * i, 8 * i + 8), 2);
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    @Tool(name = "encode_octal", description = "Encode string to octal")
    public String encodeOctal(String text) {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%03o", b & 0xFF));
        return sb.toString();
    }

    @Tool(name = "decode_octal", description = "Decode octal to string")
    public String decodeOctal(String octal) {
        byte[] bytes = new byte[octal.length() / 3];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(octal.substring(3 * i, 3 * i + 3), 8);
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    @Tool(name = "encode_rot13", description = "Encode string with ROT13")
    public String encodeRot13(String text) {
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (c >= 'a' && c <= 'z') c = (char) ((c - 'a' + 13) % 26 + 'a');
            else if (c >= 'A' && c <= 'Z') c = (char) ((c - 'A' + 13) % 26 + 'A');
            sb.append(c);
        }
        return sb.toString();
    }

    @Tool(name = "encode_ascii", description = "Convert string to ASCII codes")
    public String encodeAscii(String text) {
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) sb.append((int) c).append(" ");
        return sb.toString().trim();
    }

    @Tool(name = "decode_ascii", description = "Convert ASCII codes to string")
    public String decodeAscii(String asciiCodes) {
        StringBuilder sb = new StringBuilder();
        for (String code : asciiCodes.split("\\s+")) {
            sb.append((char) Integer.parseInt(code));
        }
        return sb.toString();
    }
}
