package com.agenthub.test.architecture;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.agenthub.test.architecture.MethodViolationDumper.Violation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 一次性工具：读取 build/method-violations.json，生成 architecture-exemptions.json。
 * <p>
 * 用法：先运行 {@code gradle generateViolations} 生成违规清单，再运行
 * {@code java ExemptionGenerator} 写入豁免配置。
 */
public class ExemptionGenerator {

    public static void main(String[] args) throws IOException {
        Path inputPath = Paths.get("build/method-violations.json");
        if (!Files.exists(inputPath)) {
            System.err.println("请先运行 gradle generateViolations 生成 " + inputPath);
            System.exit(1);
        }
        ObjectMapper mapper = new ObjectMapper();
        List<Violation> violations = Arrays.asList(mapper.readValue(inputPath.toFile(), Violation[].class));

        ArchitectureExemptions exemptions = new ArchitectureExemptions();
        List<ExemptedMethod> items = new ArrayList<>();
        for (Violation v : violations) {
            ExemptedMethod m = new ExemptedMethod();
            m.setClassName(v.fullClassName);
            m.setMethodName(v.methodName);
            m.setParameterTypes(splitParamTypes(v.paramTypes));
            m.setSeverity(v.severity);
            m.setReason(v.reason);
            items.add(m);
        }
        exemptions.setExemptedMethods(items);

        Path out = Paths.get("src/test/resources/architecture-exemptions.json");
        ObjectMapper writer = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        writer.writeValue(out.toFile(), exemptions);
        System.out.println("Wrote " + items.size() + " exemptions to " + out.toAbsolutePath());
    }

    private static List<String> splitParamTypes(String csv) {
        if (csv == null || csv.isBlank()) {
            return new ArrayList<>();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
