package com.agenthub.test.architecture;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 一次性工具：扫描所有方法复杂度违规，输出到 build/method-violations.tsv 和 method-violations.json。
 * <p>
 * 综合以下维度对违规方法分级（HEAVY/MEDIUM/LIGHT）：
 * <ul>
 *     <li>方法行数</li>
 *     <li>参数超标数量</li>
 *     <li>业务可拆分性：链式调用密度、控制流密度、简单赋值密度</li>
 *     <li>方法名特征：build/convert/map/toXxx 等"装配型"方法</li>
 * </ul>
 * 行数统计和排除规则复用 {@link MethodLineAnalyzer} 和 {@link MethodComplexityRules}，
 * 确保与 {@link AgentHubCleanArchitectureTest} 计数口径一致。
 */
public class MethodViolationDumper {

    private static final Pattern CHAINED_CALL = Pattern.compile(".*\\.[a-zA-Z_][a-zA-Z0-9_]*\\([^)]*\\)\\.[a-zA-Z_].*");
    private static final Pattern CONTROL_FLOW = Pattern.compile(".*\\b(if|else|for|while|switch|try|catch)\\b.*");
    private static final Pattern ASSIGNMENT = Pattern.compile(".*\\s*=\\s*[^=].*");
    private static final Pattern BUILDER_NAME = Pattern.compile("^(build|to|convert|map|wrap|as|of|new|create|make|compose|fill|apply)[A-Z_].*|^build$|^to[A-Z].*");

    public static void main(String[] args) throws IOException {
        JavaClasses classes = new ClassFileImporter().importPackages("com.agenthub");

        List<Violation> violations = new ArrayList<>();

        classes.stream()
                .filter(c -> !c.isInterface())
                .filter(c -> !c.getModifiers().contains(JavaModifier.ABSTRACT))
                .filter(c -> c.getPackageName().startsWith("com.agenthub"))
                .forEach(clazz -> {
                    String clazzName = clazz.getName();
                    clazz.getMethods().stream()
                            .filter(m -> m.getOwner().getName().equals(clazzName))
                            .forEach(method -> {
                                if (MethodComplexityRules.shouldSkip(method)) {
                                    return;
                                }
                                int lines = MethodLineAnalyzer.countLines(method);
                                if (lines > MethodComplexityRules.MAX_METHOD_LINES) {
                                    violations.add(analyze("LINES", lines, clazz.getFullName(), method));
                                }
                                int params = method.getParameters().size();
                                if (params > MethodComplexityRules.MAX_METHOD_PARAMS) {
                                    Violation v = analyze("PARAMS", params, clazz.getFullName(), method);
                                    v.lineCount = params;
                                    v.type = "PARAMS";
                                    violations.add(v);
                                }
                            });
                });

        violations.sort((a, b) -> {
            int r = Integer.compare(severityRank(a.severity), severityRank(b.severity));
            if (r != 0) {
                return r;
            }
            r = Integer.compare(b.lineCount, a.lineCount);
            if (r != 0) {
                return r;
            }
            r = a.fullClassName.compareTo(b.fullClassName);
            if (r != 0) {
                return r;
            }
            return a.methodName.compareTo(b.methodName);
        });

        Path out = Paths.get("build/method-violations.tsv");
        Files.createDirectories(out.getParent());
        try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(out, StandardCharsets.UTF_8))) {
            pw.println("# severity\ttype\theight\tclassName#methodName(paramTypes)\tchainPct\tcontrolPct\tassignPct\tnameHint\treason");
            for (Violation v : violations) {
                pw.printf("%s\t%s\t%d\t%s#%s(%s)\t%d\t%d\t%d\t%s\t%s%n",
                        v.severity, v.type, v.lineCount, v.fullClassName, v.methodName, v.paramTypes,
                        v.chainPct, v.controlPct, v.assignPct, v.nameHint, v.reason);
            }
        }

        Path jsonOut = Paths.get("build/method-violations.json");
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        mapper.writeValue(jsonOut.toFile(), violations);

        System.out.println("Wrote " + violations.size() + " violations to " + out.toAbsolutePath());
        System.out.println("Wrote " + violations.size() + " violations to " + jsonOut.toAbsolutePath());
        long heavy = violations.stream().filter(v -> "HEAVY".equals(v.severity)).count();
        long medium = violations.stream().filter(v -> "MEDIUM".equals(v.severity)).count();
        long light = violations.stream().filter(v -> "LIGHT".equals(v.severity)).count();
        System.out.printf("Severity: HEAVY=%d, MEDIUM=%d, LIGHT=%d%n", heavy, medium, light);
    }

    private static String paramTypes(JavaMethod method) {
        return method.getRawParameterTypes().stream()
                .map(JavaClass::getName)
                .collect(Collectors.joining(","));
    }

    private static Violation analyze(String type, int lineCount, String fullClassName, JavaMethod method) {
        Violation v = new Violation();
        v.type = type;
        v.lineCount = lineCount;
        v.fullClassName = fullClassName;
        v.methodName = method.getName();
        v.paramTypes = paramTypes(method);

        List<String> methodBody = extractBody(fullClassName, method);
        v.bodySize = methodBody.size();
        v.chainPct = methodBody.isEmpty() ? 0 : (int) Math.round(100.0 * methodBody.stream().filter(l -> CHAINED_CALL.matcher(l).matches()).count() / methodBody.size());
        v.controlPct = methodBody.isEmpty() ? 0 : (int) Math.round(100.0 * methodBody.stream().filter(l -> CONTROL_FLOW.matcher(l).matches()).count() / methodBody.size());
        v.assignPct = methodBody.isEmpty() ? 0 : (int) Math.round(100.0 * methodBody.stream().filter(l -> ASSIGNMENT.matcher(l).matches()).count() / methodBody.size());
        v.nameHint = BUILDER_NAME.matcher(method.getName()).matches();

        v.severity = computeSeverity(v);
        v.reason = buildReason(v);
        return v;
    }

    private static List<String> extractBody(String fullClassName, JavaMethod method) {
        Path sourceRoot = Paths.get(System.getProperty("user.dir"), "src/main/java");
        Path sourceFile = sourceRoot.resolve(fullClassName.replace('.', '/') + ".java");
        if (!Files.exists(sourceFile)) {
            return List.of();
        }
        int declLine = method.getSourceCodeLocation().getLineNumber();
        if (declLine <= 0) {
            return List.of();
        }
        try {
            List<String> lines = Files.readAllLines(sourceFile);
            int start = declLine - 1;
            int depth = 0;
            boolean opened = false;
            for (int i = start; i < lines.size(); i++) {
                String line = lines.get(i);
                for (int j = 0; j < line.length(); j++) {
                    char c = line.charAt(j);
                    if (c == '{') {
                        depth++;
                        opened = true;
                    } else if (c == '}') {
                        depth--;
                        if (opened && depth == 0) {
                            return new ArrayList<>(lines.subList(start, i + 1));
                        }
                    }
                }
            }
        } catch (IOException ignored) {
        }
        return List.of();
    }

    /**
     * 分级算法：
     * - LIGHT：方法名是装配型（build/convert/map/toXxx 等），
     *   或行数 ≤ 15 且 链式调用密度 ≥ 40%，
     *   或 行数 ≤ 12（轻微超标）。
     * - HEAVY：行数 > 30 且控制流密度 ≥ 20%（高内聚的复杂逻辑，难以拆分），
     *   或参数 > 5（参数严重超标，必须用 Command 封装）。
     * - MEDIUM：其余情况（可拆分为几个独立小方法）。
     */
    private static String computeSeverity(Violation v) {
        if ("PARAMS".equals(v.type)) {
            if (v.lineCount >= 5) {
                return "HEAVY";
            }
            return "MEDIUM";
        }
        if (v.nameHint && v.controlPct < 25) {
            return "LIGHT";
        }
        if (v.lineCount <= 12) {
            return "LIGHT";
        }
        if (v.lineCount >= 31 && v.controlPct >= 20) {
            return "HEAVY";
        }
        if (v.lineCount > 30) {
            return "HEAVY";
        }
        if (v.lineCount <= 15 && v.chainPct >= 40) {
            return "LIGHT";
        }
        return "MEDIUM";
    }

    private static String buildReason(Violation v) {
        StringBuilder sb = new StringBuilder();
        sb.append(v.severity).append(" 违规：");
        if ("LINES".equals(v.type)) {
            sb.append(v.lineCount).append(" 行（>10）");
        } else {
            sb.append(v.lineCount).append(" 个参数（>3）");
        }
        sb.append("，链式调用=").append(v.chainPct).append("%");
        sb.append("，控制流=").append(v.controlPct).append("%");
        sb.append("，赋值=").append(v.assignPct).append("%");
        if (v.nameHint) {
            sb.append("，方法名暗示装配型");
        }
        return sb.toString();
    }

    private static int severityRank(String s) {
        return switch (s) {
            case "HEAVY" -> 0;
            case "MEDIUM" -> 1;
            case "LIGHT" -> 2;
            default -> 3;
        };
    }

    public static class Violation {
        public String severity;
        public String type;
        public int lineCount;
        public String fullClassName;
        public String methodName;
        public String paramTypes;
        public int bodySize;
        public int chainPct;
        public int controlPct;
        public int assignPct;
        public boolean nameHint;
        public String reason;
    }
}
