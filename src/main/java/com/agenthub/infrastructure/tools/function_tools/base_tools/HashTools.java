package com.agenthub.infrastructure.tools.function_tools.base_tools;

import com.agenthub.infrastructure.tools.function_tools.annotations.AgentTools;
import org.springframework.ai.tool.annotation.Tool;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@AgentTools(name = "HashTools", description = "哈希计算工具，提供MD5、SHA-256、SHA-512等哈希算法的计算和验证功能", defaultEnable = false)
public class HashTools {

    @Tool(name = "hash_md5", description = "Calculate MD5 hash")
    public String md5(String text) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(text.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(digest);
    }

    @Tool(name = "hash_sha1", description = "Calculate SHA-1 hash")
    public String sha1(String text) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] digest = md.digest(text.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(digest);
    }

    @Tool(name = "hash_sha256", description = "Calculate SHA-256 hash")
    public String sha256(String text) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(text.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(digest);
    }

    @Tool(name = "hash_sha384", description = "Calculate SHA-384 hash")
    public String sha384(String text) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-384");
        byte[] digest = md.digest(text.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(digest);
    }

    @Tool(name = "hash_sha512", description = "Calculate SHA-512 hash")
    public String sha512(String text) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        byte[] digest = md.digest(text.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(digest);
    }

    @Tool(name = "hash_file_md5", description = "Calculate MD5 of file")
    public String fileMd5(String filePath) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] fileBytes = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(filePath));
        return bytesToHex(md.digest(fileBytes));
    }

    @Tool(name = "hash_file_sha256", description = "Calculate SHA-256 of file")
    public String fileSha256(String filePath) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] fileBytes = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(filePath));
        return bytesToHex(md.digest(fileBytes));
    }

    @Tool(name = "hash_compare", description = "Compare two hash values")
    public boolean compare(String hash1, String hash2) {
        return hash1.equalsIgnoreCase(hash2);
    }

    @Tool(name = "hash_verify", description = "Verify text against expected hash")
    public boolean verify(String text, String expectedHash, String algorithm) throws Exception {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        String calculated = bytesToHex(md.digest(text.getBytes(StandardCharsets.UTF_8)));
        return calculated.equalsIgnoreCase(expectedHash);
    }

    @Tool(name = "hash_md5_short", description = "Calculate short MD5 (first 8 chars)")
    public String md5Short(String text) throws Exception {
        return md5(text).substring(0, 8);
    }

    @Tool(name = "hash_sha256_short", description = "Calculate short SHA-256 (first 16 chars)")
    public String sha256Short(String text) throws Exception {
        return sha256(text).substring(0, 16);
    }

    @Tool(name = "hash_combine", description = "Combine multiple hashes")
    public String combine(String text1, String text2, String algorithm) throws Exception {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        md.update(text1.getBytes(StandardCharsets.UTF_8));
        md.update(text2.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(md.digest());
    }

    @Tool(name = "hash_iterative", description = "Apply hash multiple times")
    public String iterative(String text, String algorithm, int iterations) throws Exception {
        String result = text;
        for (int i = 0; i < iterations; i++) {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            result = bytesToHex(md.digest(result.getBytes(StandardCharsets.UTF_8)));
        }
        return result;
    }

    @Tool(name = "hash_hmac_simple", description = "Simple HMAC-like hash with key")
    public String hmacSimple(String text, String key, String algorithm) throws Exception {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        md.update(key.getBytes(StandardCharsets.UTF_8));
        md.update(text.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(md.digest());
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
