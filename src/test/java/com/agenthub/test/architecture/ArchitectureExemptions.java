package com.agenthub.test.architecture;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 架构豁免配置加载器。
 * <p>
 * 从 classpath 根目录的 {@code architecture-exemptions.json} 读取豁免列表，
 * 转换为 {@link MethodKey} 集合供 ArchUnit 规则使用。
 * 加载失败时返回空集合，不阻塞测试运行。
 */
class ArchitectureExemptions {

    private static final Logger log = LoggerFactory.getLogger(ArchitectureExemptions.class);
    private static final String RESOURCE_PATH = "/architecture-exemptions.json";

    private List<ExemptedMethod> exemptedMethods = new ArrayList<>();

    public List<ExemptedMethod> getExemptedMethods() {
        return exemptedMethods;
    }

    public void setExemptedMethods(List<ExemptedMethod> exemptedMethods) {
        this.exemptedMethods = exemptedMethods == null ? new ArrayList<>() : exemptedMethods;
    }

    public static ArchitectureExemptions load() {
        ClassLoader loader = ArchitectureExemptions.class.getClassLoader();
        try (InputStream in = loader.getResourceAsStream("architecture-exemptions.json")) {
            if (in == null) {
                log.warn("未找到豁免配置文件 architecture-exemptions.json，跳过豁免");
                return new ArchitectureExemptions();
            }
            return new ObjectMapper().readValue(in, ArchitectureExemptions.class);
        } catch (IOException e) {
            log.error("加载豁免配置文件失败，跳过豁免", e);
            return new ArchitectureExemptions();
        }
    }

    public Set<MethodKey> toMethodKeys() {
        if (exemptedMethods == null || exemptedMethods.isEmpty()) {
            return Collections.emptySet();
        }
        return exemptedMethods.stream()
                .map(m -> new MethodKey(m.getClassName(), m.getMethodName(), m.getParameterTypes()))
                .collect(Collectors.toSet());
    }
}
