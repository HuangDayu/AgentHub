package com.agenthub.test.common;

import cn.hutool.core.io.FileUtil;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static com.agenthub.common.utils.RandomUtils.randomId;
import static com.agenthub.infrastructure.context.TenantContextHeaders.*;


/**
 * @author huangdayu
 */
public class TestCommonTools {

    public static final String TOKEN_FILE = System.getProperty("user.dir") + "/keys/token.txt";
    public static final String WORKSPACE_ID = "100000002";
    public static final String TENANT_ID = "100000002";

    public static String getConfigTestLocation() {
        String property = System.getProperty("user.dir");
        if (property.contains("\\domain")) {
            // 最后一个反斜杠的位置
            int lastSlash = property.lastIndexOf('\\');
            // 倒数第二个反斜杠的位置
            int secondLastSlash = property.lastIndexOf('\\', lastSlash - 1);
            if (secondLastSlash != -1) {
                return property.substring(0, secondLastSlash) + "\\config\\dev\\application-dev.yml";
            }
        }
        return property + "\\config\\dev\\application-dev.yml";
    }

    public static String extractJsonValue(String json, String key) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]+)\"");
        java.util.regex.Matcher matcher = pattern.matcher(json);
        if (!matcher.find()) {
            throw new IllegalStateException("Missing key in json: " + key);
        }
        return matcher.group(1);
    }

    public static RequestBuilder getRequestBuilder() {
        String token = FileUtil.readUtf8String(TOKEN_FILE);
        return MockMvcRequestBuilders.get("/")
                .header("Authorization", token)
                .header(CONTEXT_TENANT_ID, TENANT_ID)
                .header(CONTEXT_WORKSPACE_ID, WORKSPACE_ID)
                .header(CONTEXT_REQUEST_ID, randomId());
    }

    public static void writeToken(String token) {
        FileUtil.writeUtf8String(token, TOKEN_FILE);
    }

}
