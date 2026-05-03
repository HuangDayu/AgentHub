package com.agenthub.test.schema;

/**
 * Schema 生成器命令行工具。
 */
public class SchemaGeneratorCli {

    /**
     * 主入口方法。
     */
    public static void main(String[] args) {
        try {
            String basePackage = getArg(args, "--base-package", "com.agenthub");
            String schemaOutput = getArg(args, "--schema-output", "sql/schema.sql");
            String checklistOutput = getArg(args, "--checklist-output", "sql/checklist.sql");
            printHeader(basePackage, schemaOutput, checklistOutput);
            executeGeneration(basePackage, schemaOutput, checklistOutput);
            printFooter();
        } catch (Exception e) {
            handleError(e);
        }
    }

    /**
     * 打印头部信息。
     */
    private static void printHeader(String basePackage, String schemaOutput, String checklistOutput) {
        System.out.println("========================================");
        System.out.println("Schema Generator CLI");
        System.out.println("========================================");
        System.out.println("Base package: " + basePackage);
        System.out.println("Schema output: " + schemaOutput);
        System.out.println("Checklist output: " + checklistOutput);
        System.out.println("========================================\n");
    }

    /**
     * 执行生成。
     */
    private static void executeGeneration(String basePackage, String schemaOutput, String checklistOutput) throws Exception {
        SchemaGenerator generator = new SchemaGenerator();
        generator.generate(basePackage, schemaOutput, checklistOutput);
    }

    /**
     * 打印尾部信息。
     */
    private static void printFooter() {
        System.out.println("\n========================================");
        System.out.println("Schema generation completed!");
        System.out.println("========================================");
    }

    /**
     * 处理错误。
     */
    private static void handleError(Exception e) {
        System.err.println("Error: " + e.getMessage());
        e.printStackTrace();
        System.exit(1);
    }

    /**
     * 获取命令行参数。
     */
    private static String getArg(String[] args, String name, String defaultValue) {
        for (int i = 0; i < args.length - 1; i++) {
            if (name.equals(args[i])) {
                return args[i + 1];
            }
        }
        return defaultValue;
    }
}
