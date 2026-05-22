package com.agenthub.test.agents.aliyun;

import static org.assertj.core.api.Assertions.assertThat;

import com.agenthub.infrastructure.agents.aliyun.AgentScopeSpringModelAdapter;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.model.ChatResponse;
import io.agentscope.core.model.GenerateOptions;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 验证 {@link AgentScopeSpringModelAdapter} 桥接逻辑（含消息转换）。
 */
@ExtendWith(MockitoExtension.class)
public class AgentScopeSpringModelAdapterTest {

    @Mock
    private ChatModel chatModel;

    @Test
    public void shouldConvertUserMsgAndDelegateToChatModel() {
        var springResponse = new org.springframework.ai.chat.model.ChatResponse(
                List.of(new Generation(
                        AssistantMessage.builder().content("Hello back").build())));

        when(chatModel.stream(any(Prompt.class))).thenReturn(Flux.just(springResponse));

        var adapter = new AgentScopeSpringModelAdapter("test-model", chatModel);
        List<Msg> messages = List.of(
                Msg.builder().role(MsgRole.USER).textContent("Hi").build());

        Flux<ChatResponse> result = adapter.stream(messages, null,
                GenerateOptions.builder().build());

        StepVerifier.create(result)
                .assertNext(resp -> {
                    assertThat(resp.getContent()).isNotEmpty();
                    var block = resp.getContent().get(0);
                    assertThat(block).isInstanceOf(TextBlock.class);
                    assertThat(((TextBlock) block).getText()).isEqualTo("Hello back");
                })
                .verifyComplete();

        verify(chatModel).stream(any(Prompt.class));
    }

    @Test
    public void shouldConvertSystemMsg() {
        var springResponse = new org.springframework.ai.chat.model.ChatResponse(
                List.of(new Generation(
                        AssistantMessage.builder().content("ACK").build())));

        when(chatModel.stream(any(Prompt.class))).thenReturn(Flux.just(springResponse));

        var adapter = new AgentScopeSpringModelAdapter("m", chatModel);
        List<Msg> messages = List.of(
                Msg.builder().role(MsgRole.SYSTEM).textContent("be helpful").build(),
                Msg.builder().role(MsgRole.USER).textContent("hey").build());

        Flux<ChatResponse> result = adapter.stream(messages, null, null);

        StepVerifier.create(result)
                .assertNext(resp -> assertThat(
                        ((TextBlock) resp.getContent().get(0)).getText()).isEqualTo("ACK"))
                .verifyComplete();
    }

    @Test
    public void shouldReturnModelName() {
        var adapter = new AgentScopeSpringModelAdapter("my-model", chatModel);
        assertThat(adapter.getModelName()).isEqualTo("my-model");
    }

}
