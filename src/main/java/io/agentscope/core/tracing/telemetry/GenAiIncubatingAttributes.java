/*
 * Copyright 2024-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.agentscope.core.tracing.telemetry;

import io.opentelemetry.api.common.AttributeKey;

import java.util.List;

import static io.opentelemetry.api.common.AttributeKey.*;

/**
 * Copied from <a href=https://github.com/open-telemetry/semantic-conventions/blob/v1.37.0/model/gen-ai/registry.yaml>OpenTelemetry semantic conventions 1.37.0</a>.
 *
 */
public final class GenAiIncubatingAttributes {

    public static final AttributeKey<String> AGENTHUB_TENANT_ID = stringKey("agenthub.context.tenantId");
    public static final AttributeKey<String> AGENTHUB_WORKSPACE_ID = stringKey("agenthub.context.workspaceId");
    public static final AttributeKey<String> AGENTHUB_AGENT_ID = stringKey("agenthub.context.agentId");
    public static final AttributeKey<String> AGENTHUB_SESSION_ID = stringKey("agenthub.context.sessionId");


    public static final AttributeKey<String> GEN_AI_INPUT_MESSAGES = stringKey("gen_ai.input.messages");

    public static final AttributeKey<String> GEN_AI_OUTPUT_MESSAGES = stringKey("gen_ai.output.messages");

    public static final AttributeKey<String> GEN_AI_TOOL_DEFINITIONS =
            stringKey("gen_ai.tool.definitions");

    public static final AttributeKey<String> GEN_AI_SYSTEM_INSTRUCTIONS =
            stringKey("gen_ai.system_instructions");

    public static final AttributeKey<String> GEN_AI_AGENT_DESCRIPTION =
            stringKey("gen_ai.agent.description");

    public static final AttributeKey<String> GEN_AI_AGENT_ID = stringKey("gen_ai.agent.id");

    public static final AttributeKey<String> GEN_AI_AGENT_NAME = stringKey("gen_ai.agent.name");

    public static final AttributeKey<String> GEN_AI_CONVERSATION_ID = stringKey("gen_ai.conversation.id");

    public static final AttributeKey<String> GEN_AI_DATA_SOURCE_ID = stringKey("gen_ai.data_source.id");

    public static final AttributeKey<String> GEN_AI_OPERATION_NAME = stringKey("gen_ai.operation.name");

    public static final AttributeKey<String> GEN_AI_OUTPUT_TYPE = stringKey("gen_ai.output.type");

    public static final AttributeKey<String> GEN_AI_PROVIDER_NAME = stringKey("gen_ai.provider.name");

    public static final AttributeKey<Long> GEN_AI_REQUEST_CHOICE_COUNT =
            longKey("gen_ai.request.choice.count");

    public static final AttributeKey<List<String>> GEN_AI_REQUEST_ENCODING_FORMATS =
            stringArrayKey("gen_ai.request.encoding_formats");

    public static final AttributeKey<Double> GEN_AI_REQUEST_FREQUENCY_PENALTY =
            doubleKey("gen_ai.request.frequency_penalty");

    public static final AttributeKey<Long> GEN_AI_REQUEST_MAX_TOKENS =
            longKey("gen_ai.request.max_tokens");

    public static final AttributeKey<String> GEN_AI_REQUEST_MODEL = stringKey("gen_ai.request.model");

    public static final AttributeKey<Double> GEN_AI_REQUEST_PRESENCE_PENALTY =
            doubleKey("gen_ai.request.presence_penalty");

    public static final AttributeKey<Long> GEN_AI_REQUEST_SEED = longKey("gen_ai.request.seed");

    public static final AttributeKey<List<String>> GEN_AI_REQUEST_STOP_SEQUENCES =
            stringArrayKey("gen_ai.request.stop_sequences");

    public static final AttributeKey<Double> GEN_AI_REQUEST_TEMPERATURE =
            doubleKey("gen_ai.request.temperature");

    public static final AttributeKey<Double> GEN_AI_REQUEST_TOP_K = doubleKey("gen_ai.request.top_k");

    public static final AttributeKey<Double> GEN_AI_REQUEST_TOP_P = doubleKey("gen_ai.request.top_p");

    public static final AttributeKey<List<String>> GEN_AI_RESPONSE_FINISH_REASONS =
            stringArrayKey("gen_ai.response.finish_reasons");

    public static final AttributeKey<String> GEN_AI_RESPONSE_ID = stringKey("gen_ai.response.id");

    public static final AttributeKey<String> GEN_AI_RESPONSE_MODEL = stringKey("gen_ai.response.model");

    public static final AttributeKey<String> GEN_AI_TOKEN_TYPE = stringKey("gen_ai.token.type");

    public static final AttributeKey<String> GEN_AI_TOOL_CALL_ID = stringKey("gen_ai.tool.call.id");

    public static final AttributeKey<String> GEN_AI_TOOL_DESCRIPTION =
            stringKey("gen_ai.tool.description");

    public static final AttributeKey<String> GEN_AI_TOOL_NAME = stringKey("gen_ai.tool.name");

    public static final AttributeKey<String> GEN_AI_TOOL_TYPE = stringKey("gen_ai.tool.type");

    public static final AttributeKey<String> GEN_AI_TOOL_CALL_ARGUMENTS =
            stringKey("gen_ai.tool.call.arguments");

    public static final AttributeKey<String> GEN_AI_TOOL_CALL_RESULT =
            stringKey("gen_ai.tool.call.result");

    public static final AttributeKey<Long> GEN_AI_USAGE_INPUT_TOKENS =
            longKey("gen_ai.usage.input_tokens");

    public static final AttributeKey<Long> GEN_AI_USAGE_OUTPUT_TOKENS =
            longKey("gen_ai.usage.output_tokens");

    public static final class GenAiOperationNameIncubatingValues {
        public static final String CHAT = "chat";

        public static final String GENERATE_CONTENT = "generate_content";

        public static final String TEXT_COMPLETION = "text_completion";

        public static final String EMBEDDINGS = "embeddings";

        public static final String CREATE_AGENT = "create_agent";

        public static final String INVOKE_AGENT = "invoke_agent";

        public static final String EXECUTE_TOOL = "execute_tool";

        private GenAiOperationNameIncubatingValues() {
        }
    }

    public static final class GenAiOutputTypeIncubatingValues {
        public static final String TEXT = "text";

        public static final String JSON = "json";

        public static final String IMAGE = "image";

        public static final String SPEECH = "speech";

        private GenAiOutputTypeIncubatingValues() {
        }
    }

    public static final class GenAiProviderNameIncubatingValues {
        public static final String OPENAI = "openai";

        public static final String GCP_GEN_AI = "gcp.gen_ai";

        public static final String GCP_VERTEX_AI = "gcp.vertex_ai";

        public static final String GCP_GEMINI = "gcp.gemini";

        public static final String ANTHROPIC = "anthropic";

        public static final String COHERE = "cohere";

        public static final String AZURE_AI_INFERENCE = "azure.ai.inference";

        public static final String AZURE_AI_OPENAI = "azure.ai.openai";

        public static final String IBM_WATSONX_AI = "ibm.watsonx.ai";

        public static final String AWS_BEDROCK = "aws.bedrock";

        public static final String PERPLEXITY = "perplexity";

        public static final String X_AI = "x_ai";

        public static final String DEEPSEEK = "deepseek";

        public static final String GROQ = "groq";

        public static final String MISTRAL_AI = "mistral_ai";

        private GenAiProviderNameIncubatingValues() {
        }
    }

    public static final class GenAiTokenTypeIncubatingValues {
        public static final String INPUT = "input";

        public static final String OUTPUT = "output";

        private GenAiTokenTypeIncubatingValues() {
        }
    }

    private GenAiIncubatingAttributes() {
    }
}
