package com.agenthub.test.common;

import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static com.agenthub.common.utils.RandomUtils.randomId;
import static com.agenthub.common.context.TenantContextHeaders.*;


/**
 * @author huangdayu
 */
public class TestCommonTools {


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
        return MockMvcRequestBuilders.get("/")
                .header("Authorization", "Bearer eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJsaXNpIiwidGVuYW50SWQiOiIxMDAwMDAwMDIiLCJyb2xlcyI6WyJST0xFX0FETUlOIl0sImlhdCI6MTc3NzI2MTA3NSwiZXhwIjoxNzc3OTgxMDc1fQ.-_HFZ686n5MR8MrNdyYcooMBDquJPzalmv9r79vRl3HhIzP5c1LadB-gl6V6m-Vn")
                .header(CONTEXT_TENANT_ID, "100000002")
                .header(CONTEXT_WORKSPACE_ID, "100000002")
                .header(CONTEXT_REQUEST_ID, randomId());
    }

}
