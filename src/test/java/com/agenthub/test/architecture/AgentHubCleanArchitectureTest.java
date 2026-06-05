package com.agenthub.test.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * 整洁架构规则测试
 * 确保domain模块符合整洁架构的依赖规则
 * <p>
 * 使用ClassFileImporter只导入项目内部的类，避免扫描外部依赖
 */
public class AgentHubCleanArchitectureTest {

    /**
     * 导入项目内部的类，排除外部依赖
     */
    private static final JavaClasses classes = new ClassFileImporter()
            .importPackages("com.agenthub");

    /**
     * 豁免的方法集合：从 classpath 中 architecture-exemptions.json 加载，
     * 命中此集合的方法将被两个方法复杂度测试跳过。
     */
    private static final Set<MethodKey> EXEMPTED_METHOD_KEYS = ArchitectureExemptions.load().toMethodKeys();

    // ==================== 架构依赖规则 ====================

    /**
     * 领域层不应该依赖任何外层
     */
    @Test
    void domain_should_not_depend_on_outer_layers() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..application..", "..infrastructure..", "..api..")
                .because("领域层应该是核心层，不应该依赖应用层、基础设施层或API层")
                .check(classes);
    }

    /**
     * 应用层不应该依赖基础设施层或API层
     */
    @Test
    void application_should_not_depend_on_infrastructure_or_api() {
        noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..infrastructure..", "..api..")
                .because("应用层只应该依赖领域层，不应该依赖基础设施层或API层")
                .check(classes);
    }

    /**
     * API层不应该依赖基础设施层
     */
    @Test
    void api_should_not_depend_on_infrastructure() {
        noClasses()
                .that().resideInAPackage("..api..")
                .should().dependOnClassesThat()
                .resideInAPackage("com.agenthub..infrastructure..")
                .because("API层不应该依赖基础设施层")
                .check(classes);
    }


    /**
     * 基础设施层不应该依赖API层
     */
    @Test
    void infrastructure_should_not_depend_on_api() {
        noClasses()
                .that().resideInAPackage("..infrastructure..")
                .should().dependOnClassesThat()
                .resideInAPackage("com.agenthub..api..")
                .because("基础设施层不应该依赖API层")
                .check(classes);
    }

    /**
     * API层不应该直接依赖领域层（应该通过应用层）
     */
    @Test
    void api_should_not_directly_depend_on_domain() {
        noClasses()
                .that().resideInAPackage("..api.controller..")
                .should().dependOnClassesThat()
                .resideInAPackage("..domain..")
                .because("API控制器应该通过应用层访问领域逻辑，不应该直接依赖领域层")
                .check(classes);
    }

    /**
     * 领域模型应该是POJO（不依赖框架）
     */
    @Test
    void domain_models_should_be_pojos() {
        noClasses()
                .that().resideInAPackage("..domain.model..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("org.springframework..", "jakarta..", "org.apache..")
                .because("领域模型应该是纯POJO，不应该依赖任何框架")
                .check(classes);
    }

    /**
     * 领域用例应该只依赖领域模型
     */
    @Test
    void domain_use_case_should_only_depend_on_domain() {
        classes()
                .that().resideInAPackage("..domain.usecase..")
                .should().onlyDependOnClassesThat()
                .resideInAnyPackage("..domain..", "java..", "org.slf4j..")
                .allowEmptyShould(true)
                .because("领域服务应该只依赖领域模型和标准库")
                .check(classes);
    }

    /**
     * 应用用例命名约定
     */
    @Test
    void application_use_case_naming_convention() {
        classes()
                .that().resideInAPackage("..application.usecase..")
                .and().areNotInterfaces()
                .and().areNotAnonymousClasses()
                .should().haveSimpleNameEndingWith("UseCase")
                .because("应用服务类应该以'UseCase'结尾")
                .check(classes);
    }

    /**
     * 仓储接口应该在应用层定义
     */
    @Test
    void repository_interfaces_should_be_in_application() {
        classes()
                .that().haveSimpleNameEndingWith("Repository")
                .and().areInterfaces()
                .should().resideInAPackage("..application.port.out..")
                .because("仓储接口（出端口）应该在应用层的port.out包中定义")
                .check(classes);
    }

    /**
     * 仓储实现应该在基础设施层
     */
    @Test
    void repository_implementations_should_be_in_infrastructure() {
        classes()
                .that().haveSimpleNameStartingWith("Mybatis").and().haveSimpleNameEndingWith("Repository")
                .should().resideInAPackage("..infrastructure.store.db.repository..")
                .because("仓储实现应该在基础设施层的store.db.repository包中")
                .allowEmptyShould(true)
                .check(classes);
    }

    /**
     * 控制器命名约定
     */
    @Test
    void controller_naming_convention() {
        classes()
                .that().resideInAPackage("..api.controller..")
                .and().areNotInterfaces()
                .should().haveSimpleNameEndingWith("Controller")
                .because("控制器类应该以'Controller'结尾")
                .check(classes);
    }

    /**
     * DTO应该在api.dto包中
     */
    @Test
    void dtos_should_be_in_api_dto_package() {
        classes()
                .that().haveSimpleNameEndingWith("Request")
                .or().haveSimpleNameEndingWith("Response")
                .or().haveSimpleNameEndingWith("DTO")
                .should().resideInAnyPackage("..api.dto..","..infrastructure..dto..")
                .because("DTO类应该在api.dto包中")
                .check(classes);
    }

    /**
     * 领域事件应该在domain.event包中
     */
    @Test
    void domain_events_should_be_in_domain_event_package() {
        classes()
                .that().haveSimpleNameEndingWith("Event")
                .and().doNotHaveModifier(com.tngtech.archunit.core.domain.JavaModifier.ABSTRACT)
                .should().resideInAPackage("..domain.event..")
                .allowEmptyShould(true)
                .because("领域事件应该在domain.event包中")
                .check(classes);
    }

    /**
     * 应用层不应该有循环依赖
     */
    @Test
    void no_cycles_in_application_layer() {
        slices().matching("..application.(*)..")
                .should().beFreeOfCycles()
                .because("应用层不应该有循环依赖")
                .check(classes);
    }

    /**
     * 领域层不应该有循环依赖
     */
    @Test
    void no_cycles_in_domain_layer() {
        slices().matching("..domain.(*)..")
                .should().beFreeOfCycles()
                .because("领域层不应该有循环依赖")
                .check(classes);
    }

    // ==================== 方法复杂度规则 ====================

    /**
     * 业务方法不应该超过10行
     * <p>
     * 排除：构造方法、转换方法（toXxx/fromXxx）、getter/setter、builder、toString/equals/hashCode
     * <p>
     * 豁免列表：从 {@code architecture-exemptions.json} 加载已通过审查但暂不重构的方法。
     */
    @Test
    void methods_should_not_exceed_ten_lines() {
        methods()
                .that().areDeclaredInClassesThat().areNotInterfaces()
                .and().areDeclaredInClassesThat().resideInAPackage("com.agenthub..")
                .should(new ArchCondition<JavaMethod>("不超过" + MethodComplexityRules.MAX_METHOD_LINES + "行代码") {
                    @Override
                    public void check(JavaMethod method, ConditionEvents events) {
                        if (MethodComplexityRules.shouldSkip(method) || isExempted(method)) {
                            return;
                        }
                        int lineCount = MethodLineAnalyzer.countLines(method);
                        if (lineCount > MethodComplexityRules.MAX_METHOD_LINES) {
                            String message = String.format(
                                    "%s.%s() 有 %d 行代码，超过了最大允许的 %d 行 [%s]",
                                    method.getOwner().getSimpleName(),
                                    method.getName(),
                                    lineCount,
                                    MethodComplexityRules.MAX_METHOD_LINES,
                                    method.getSourceCodeLocation());
                            events.add(SimpleConditionEvent.violated(method, message));
                        }
                    }
                })
                .because("业务方法应该保持简洁，不超过" + MethodComplexityRules.MAX_METHOD_LINES + "行")
                .check(classes);
    }

    /**
     * 业务方法参数不应该超过3个
     * <p>
     * 排除：构造方法、转换方法（toXxx/fromXxx）、getter/setter、builder、toString/equals/hashCode
     * <p>
     * 豁免列表：从 {@code architecture-exemptions.json} 加载已通过审查但暂不重构的方法。
     */
    @Test
    void methods_should_not_have_more_than_three_parameters() {
        methods()
                .that().areDeclaredInClassesThat().areNotInterfaces()
                .and().areDeclaredInClassesThat().resideInAPackage("com.agenthub..")
                .should(new ArchCondition<JavaMethod>("参数不超过" + MethodComplexityRules.MAX_METHOD_PARAMS + "个") {
                    @Override
                    public void check(JavaMethod method, ConditionEvents events) {
                        if (MethodComplexityRules.shouldSkip(method) || isExempted(method)) {
                            return;
                        }
                        int paramCount = method.getParameters().size();
                        if (paramCount > MethodComplexityRules.MAX_METHOD_PARAMS) {
                            String message = String.format(
                                    "%s.%s() 有 %d 个参数，超过了最大允许的 %d 个 [%s]",
                                    method.getOwner().getSimpleName(),
                                    method.getName(),
                                    paramCount,
                                    MethodComplexityRules.MAX_METHOD_PARAMS,
                                    method.getSourceCodeLocation());
                            events.add(SimpleConditionEvent.violated(method, message));
                        }
                    }
                })
                .because("业务方法参数不应超过" + MethodComplexityRules.MAX_METHOD_PARAMS + "个，应使用Command/DTO对象封装")
                .check(classes);
    }

    // ==================== 辅助方法 ====================

    /**
     * 判断方法是否在豁免配置中（人工审查通过但暂不重构）
     */
    private static boolean isExempted(JavaMethod method) {
        if (EXEMPTED_METHOD_KEYS.isEmpty()) {
            return false;
        }
        String className = method.getOwner().getFullName();
        String methodName = method.getName();
        List<String> paramTypes = method.getRawParameterTypes().stream()
                .map(com.tngtech.archunit.core.domain.JavaClass::getName)
                .collect(Collectors.toList());
        return EXEMPTED_METHOD_KEYS.contains(new MethodKey(className, methodName, paramTypes));
    }
}
