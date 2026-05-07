package com.agenthub.test.integration;

import cn.hutool.core.io.FileUtil;
import com.agenthub.infrastructure.tools.system_tools.SystemToolsFactory;
import com.agenthub.test.TestAgentHubApplication;
import com.agenthub.infrastructure.agents.ali.saver.PostgresSaver;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.AgentHook;
import com.alibaba.cloud.ai.graph.agent.hook.HookPosition;
import com.alibaba.cloud.ai.graph.agent.hook.HookPositions;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolCallHandler;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolCallRequest;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolCallResponse;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolInterceptor;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author huangdayu
 */
@Slf4j
@SpringBootTest(classes = TestAgentHubApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        useMainMethod = SpringBootTest.UseMainMethod.ALWAYS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SpringAiAlibabaReActAgentIntegrationTest {

    protected ChatModel chatModel;
    protected String systemPrompt;

    @Resource
    private DataSource dataSource;

    @Resource
    private SystemToolsFactory systemToolsFactory;

    @SneakyThrows
    @Test
    void initChatModel() {
        chatModel = createChatModel();
        ChatMemory memory = MessageWindowChatMemory.builder().build();
        systemPrompt = FileUtil.readUtf8String(System.getProperty("user.dir") + "/CLAUDE_ZH.md");

        ReactAgent agent = ReactAgent.builder()
                .name("my_agent")
                .model(chatModel)
//                .outputType(StreamingOutput.class)
                .tools(new ArrayList<>(systemToolsFactory.getAllToolCallbacks()))
                .systemPrompt(systemPrompt)
                .saver(PostgresSaver.builder().datasource(dataSource).createTables(false).dropTablesFirst(false).build())
                .hooks(new LoggingHook())
                .interceptors(new ToolMonitoringInterceptor())
                .build();

        Flux<Message> messageFlux = agent.streamMessages("广州的天气怎么样？");
        messageFlux.subscribe(message -> {
            try {
                String text = message.getText();
                if (text != null && !text.isEmpty()) {
                    System.out.printf(text);
                }
            } catch (Exception ignored) {

            }
        });


//        Flux<NodeOutput> stream = agent.stream("广州的天气怎么样？");
//        stream.subscribe(
//                output -> {
//                    // 检查是否为 StreamingOutput 类型
//                    if (output instanceof StreamingOutput streamingOutput) {
//                        OutputType type = streamingOutput.getOutputType();
//
//                        // 处理模型推理的流式输出
//                        if (type == OutputType.AGENT_MODEL_STREAMING) {
//                            // 流式增量内容，逐步显示
//                            System.out.print(streamingOutput.message().getText());
//                        } else if (type == OutputType.AGENT_MODEL_FINISHED) {
//                            // 模型推理完成，可获取完整响应
//                            log.info("\n模型输出完成");
//                        }
//
//                        // 处理工具调用完成（目前不支持 STREAMING）
//                        if (type == OutputType.AGENT_TOOL_FINISHED) {
//                            log.info("工具调用完成: " + output.node());
//                        }
//
//                        // 对于 Hook 节点，通常只关注完成事件（如果Hook没有有效输出可以忽略）
//                        if (type == OutputType.AGENT_HOOK_FINISHED) {
//                            log.info("Hook 执行完成: " + output.node());
//                        }
//                    }
//                },
//                error -> System.err.println("错误: " + error),
//                () -> log.info("Agent 执行完成")
//        );

        TimeUnit.MINUTES.sleep(2);

    }

    private ChatModel createChatModel() {
        return OpenAiChatModel.builder()
                .openAiApi(OpenAiApi.builder()
                        .apiKey(FileUtil.readUtf8String(System.getProperty("user.dir") + "/key.txt"))
                        .baseUrl("https://openrouter.ai/api")
                        .build())
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("tencent/hy3-preview:free")
                        .build())
                .build();
    }

    @HookPositions({HookPosition.BEFORE_AGENT, HookPosition.AFTER_AGENT})
    public static class LoggingHook extends AgentHook {
        @Override
        public String getName() {
            return "LoggingHook";
        }

        @Override
        public CompletableFuture<Map<String, Object>> beforeAgent(OverAllState state, RunnableConfig config) {
            log.info("Agent 开始执行");
            return CompletableFuture.completedFuture(config.context());
        }

        @Override
        public CompletableFuture<Map<String, Object>> afterAgent(OverAllState state, RunnableConfig config) {
            log.info("Agent 执行完成");
            return CompletableFuture.completedFuture(config.context());
        }
    }

    public static class ToolMonitoringInterceptor extends ToolInterceptor {
        @Override
        public ToolCallResponse interceptToolCall(ToolCallRequest request, ToolCallHandler handler) {
            long startTime = System.currentTimeMillis();
            try {
                ToolCallResponse response = handler.call(request);
                log.info("工具【{}】执行成功，耗时：{}", request.getToolName(), System.currentTimeMillis() - startTime);
                return response;
            } catch (Exception e) {
                log.error("工具【{}】执行失败，耗时：{} ,异常：", request.getToolName(), System.currentTimeMillis() - startTime, e);
                return ToolCallResponse.error(request.getToolCallId(), request.getToolName(), "工具执行遇到问题，请稍后重试");
            }
        }

        @Override
        public String getName() {
            return "ToolMonitoringInterceptor";
        }
    }

}
