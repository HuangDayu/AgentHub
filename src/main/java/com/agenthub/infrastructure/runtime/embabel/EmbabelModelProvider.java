package com.agenthub.infrastructure.runtime.embabel;

import com.embabel.agent.spi.LlmService;
import com.embabel.common.ai.model.*;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * @author huangdayu
 */
public class EmbabelModelProvider  implements ModelProvider {
    @Override
    public @NonNull LlmService<?> getLlm(@NonNull ModelSelectionCriteria criteria) throws NoSuitableModelException {
        return null;
    }

    @Override
    public @NonNull EmbeddingService getEmbeddingService(@NonNull ModelSelectionCriteria criteria) throws NoSuitableModelException {
        return null;
    }

    @Override
    public @NonNull List<String> listRoles(@NonNull Class<?> modelClass) {
        return List.of();
    }

    @Override
    public @NonNull List<String> listModelNames(@NonNull Class<?> modelClass) {
        return List.of();
    }

    @Override
    public @NonNull List<ModelMetadata> listModels() {
        return List.of();
    }

    @Override
    public @NonNull String infoString(@Nullable Boolean verbose, int indent) {
        return "";
    }
}
