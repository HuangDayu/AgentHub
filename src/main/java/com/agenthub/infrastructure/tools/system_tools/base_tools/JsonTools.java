package com.agenthub.infrastructure.tools.system_tools.base_tools;

import com.agenthub.infrastructure.tools.system_tools.annotations.AgentTools;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.tool.annotation.Tool;

import java.util.Iterator;

@AgentTools(name = "JsonTools", description = "JSON处理工具，提供JSON解析、查询、修改、合并、格式化等JSON操作功能", defaultEnable = false)
public class JsonTools {

    private final ObjectMapper mapper = new ObjectMapper();

    @Tool(name = "json_parse", description = "Parse JSON string")
    public String parse(String json) throws Exception {
        JsonNode node = mapper.readTree(json);
        return node.toPrettyString();
    }

    @Tool(name = "json_get", description = "Get value by path (dot notation)")
    public String get(String json, String path) throws Exception {
        JsonNode node = mapper.readTree(json);
        for (String key : path.split("\\.")) {
            if (node.isArray()) node = node.get(Integer.parseInt(key));
            else node = node.get(key);
        }
        return node.asText();
    }

    @Tool(name = "json_keys", description = "Get all keys from JSON object")
    public String keys(String json) throws Exception {
        JsonNode node = mapper.readTree(json);
        StringBuilder sb = new StringBuilder();
        Iterator<String> it = node.fieldNames();
        while (it.hasNext()) sb.append(it.next()).append("\n");
        return sb.toString();
    }

    @Tool(name = "json_values", description = "Get all values from JSON object")
    public String values(String json) throws Exception {
        JsonNode node = mapper.readTree(json);
        StringBuilder sb = new StringBuilder();
        node.forEach(n -> sb.append(n.asText()).append("\n"));
        return sb.toString();
    }

    @Tool(name = "json_size", description = "Get number of elements")
    public int size(String json) throws Exception {
        JsonNode node = mapper.readTree(json);
        return node.size();
    }

    @Tool(name = "json_is_object", description = "Check if JSON is object")
    public boolean isObject(String json) throws Exception {
        return mapper.readTree(json).isObject();
    }

    @Tool(name = "json_is_array", description = "Check if JSON is array")
    public boolean isArray(String json) throws Exception {
        return mapper.readTree(json).isArray();
    }

    @Tool(name = "json_has_key", description = "Check if key exists")
    public boolean hasKey(String json, String key) throws Exception {
        return mapper.readTree(json).has(key);
    }

    @Tool(name = "json_get_array_element", description = "Get element at index")
    public String getArrayElement(String json, int index) throws Exception {
        JsonNode node = mapper.readTree(json);
        return node.get(index).toString();
    }

    @Tool(name = "json_merge", description = "Merge two JSON objects")
    public String merge(String json1, String json2) throws Exception {
        JsonNode node1 = mapper.readTree(json1);
        JsonNode node2 = mapper.readTree(json2);
        return mapper.writeValueAsString(mapper.updateValue(node1, node2));
    }

    @Tool(name = "json_minify", description = "Minify JSON (remove whitespace)")
    public String minify(String json) throws Exception {
        JsonNode node = mapper.readTree(json);
        return mapper.writeValueAsString(node);
    }

    @Tool(name = "json_prettify", description = "Prettify JSON with indentation")
    public String prettify(String json) throws Exception {
        JsonNode node = mapper.readTree(json);
        return node.toPrettyString();
    }

    @Tool(name = "json_type", description = "Get type of JSON value")
    public String type(String json) throws Exception {
        JsonNode node = mapper.readTree(json);
        return node.getNodeType().name();
    }

    @Tool(name = "json_is_null", description = "Check if value is null")
    public boolean isNull(String json) throws Exception {
        return mapper.readTree(json).isNull();
    }

    @Tool(name = "json_depth", description = "Get nesting depth")
    public int depth(String json) throws Exception {
        return calculateDepth(mapper.readTree(json));
    }

    private int calculateDepth(JsonNode node) {
        if (!node.isContainerNode()) return 0;
        int maxChild = 0;
        for (JsonNode child : node) maxChild = Math.max(maxChild, calculateDepth(child));
        return 1 + maxChild;
    }

    @Tool(name = "json_flatten", description = "Flatten nested JSON to single level")
    public String flatten(String json) throws Exception {
        JsonNode node = mapper.readTree(json);
        java.util.Map<String, String> flat = new java.util.LinkedHashMap<>();
        flattenNode("", node, flat);
        return mapper.writeValueAsString(flat);
    }

    private void flattenNode(String prefix, JsonNode node, java.util.Map<String, String> flat) {
        if (node.isObject()) {
            node.fields().forEachRemaining(e -> flattenNode(prefix + "." + e.getKey(), e.getValue(), flat));
        } else if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                flattenNode(prefix + "[" + i + "]", node.get(i), flat);
            }
        } else {
            flat.put(prefix.substring(1), node.asText());
        }
    }
}
