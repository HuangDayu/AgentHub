package com.agenthub.infrastructure.tools.base_tools;

import com.agenthub.infrastructure.tools.annotations.AgentTools;
import org.springframework.ai.tool.annotation.Tool;

import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;

@AgentTools(defaultEnable = false)
public class RandomTools {

    private final Random random = new Random();
    private final SecureRandom secureRandom = new SecureRandom();

    @Tool(name = "random_int", description = "Generate random integer in range")
    public int randomInt(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }

    @Tool(name = "random_long", description = "Generate random long")
    public long randomLong() {
        return random.nextLong();
    }

    @Tool(name = "random_double", description = "Generate random double between 0 and 1")
    public double randomDouble() {
        return random.nextDouble();
    }

    @Tool(name = "random_double_range", description = "Generate random double in range")
    public double randomDoubleRange(double min, double max) {
        return min + (max - min) * random.nextDouble();
    }

    @Tool(name = "random_float", description = "Generate random float between 0 and 1")
    public float randomFloat() {
        return random.nextFloat();
    }

    @Tool(name = "random_boolean", description = "Generate random boolean")
    public boolean randomBoolean() {
        return random.nextBoolean();
    }

    @Tool(name = "random_bytes", description = "Generate random bytes as hex")
    public String randomBytes(int length) {
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    @Tool(name = "random_uuid", description = "Generate random UUID")
    public String randomUuid() {
        return UUID.randomUUID().toString();
    }

    @Tool(name = "random_uuid_no_dash", description = "Generate UUID without dashes")
    public String randomUuidNoDash() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    @Tool(name = "random_string", description = "Generate random alphanumeric string")
    public String randomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    @Tool(name = "random_string_chars", description = "Generate random string from chars")
    public String randomStringChars(int length, String chars) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    @Tool(name = "random_password", description = "Generate random password")
    public String randomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    @Tool(name = "random_hex", description = "Generate random hex string")
    public String randomHex(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(Integer.toHexString(random.nextInt(16)));
        }
        return sb.toString();
    }

    @Tool(name = "random_secure_int", description = "Generate cryptographically secure random int")
    public int secureRandomInt(int min, int max) {
        return secureRandom.nextInt(max - min + 1) + min;
    }

    @Tool(name = "random_secure_bytes", description = "Generate secure random bytes as hex")
    public String secureRandomBytes(int length) {
        byte[] bytes = new byte[length];
        secureRandom.nextBytes(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    @Tool(name = "random_gaussian", description = "Generate Gaussian distributed random")
    public double randomGaussian() {
        return random.nextGaussian();
    }

    @Tool(name = "random_choice", description = "Random choice from comma-separated options")
    public String randomChoice(String options) {
        String[] items = options.split(",");
        return items[random.nextInt(items.length)].trim();
    }

    @Tool(name = "random_shuffle", description = "Shuffle comma-separated items")
    public String randomShuffle(String items) {
        String[] arr = items.split(",");
        for (int i = arr.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            String temp = arr[i];
            arr[i] = arr[j];
            arr[j] = temp;
        }
        return String.join(",", arr);
    }
}
