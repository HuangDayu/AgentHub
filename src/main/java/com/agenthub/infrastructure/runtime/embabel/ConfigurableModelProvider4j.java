package com.agenthub.infrastructure.runtime.embabel;

import com.embabel.agent.spi.LlmService;
import com.embabel.common.ai.model.*;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Java implementation of ConfigurableModelProvider
 * 
 * @author huangdayu
 */
@Slf4j
public class ConfigurableModelProvider4j implements ModelProvider {

    private final List<LlmService<?>> llms;
    private final List<EmbeddingService> embeddingServices;
    private final ConfigurableModelProviderProperties properties;
    private final LlmService<?> defaultLlm;

    public ConfigurableModelProvider4j(
            List<LlmService<?>> llms,
            List<EmbeddingService> embeddingServices,
            ConfigurableModelProviderProperties properties) {
        this.llms = llms;
        this.embeddingServices = embeddingServices;
        this.properties = properties;
        this.defaultLlm = initializeDefaultLlm();
        validateLlmConfigurations();
        log.info(infoString(true, 0));
        validateEmbeddingServiceConfigurations();
    }

    private LlmService<?> initializeDefaultLlm() {
        return llms.stream()
                .filter(llm -> llm.getName().equals(properties.getDefaultLlm()))
                .findFirst()
                .orElseThrow(() -> createDefaultLlmNotFoundException());
    }

    private IllegalArgumentException createDefaultLlmNotFoundException() {
        return new IllegalArgumentException(
                "Default LLM '" + properties.getDefaultLlm() + 
                "' not found in available models: " + getLlmNames());
    }

    private List<String> getLlmNames() {
        return llms.stream().map(LlmService::getName).collect(Collectors.toList());
    }

    private void validateLlmConfigurations() {
        properties.getLlms().forEach(this::validateLlmForRole);
    }

    private void validateLlmForRole(String role, String model) {
        if (llms.stream().noneMatch(llm -> llm.getName().equals(model))) {
            throw new IllegalStateException(
                    "LLM '" + model + "' for role " + role + 
                    " is not available: Choices are " + getLlmNames());
        }
    }

    private void validateEmbeddingServiceConfigurations() {
        properties.getEmbeddingServices().forEach(this::validateEmbeddingServiceForRole);
    }

    private void validateEmbeddingServiceForRole(String role, String model) {
        if (embeddingServices.stream().noneMatch(es -> es.getName().equals(model))) {
            throw new IllegalStateException(
                    "Embedding model '" + model + "' for role " + role + 
                    " is not available: Choices are " + getEmbeddingServiceNames());
        }
    }

    private List<String> getEmbeddingServiceNames() {
        return embeddingServices.stream().map(EmbeddingService::getName).collect(Collectors.toList());
    }

    private EmbeddingService defaultEmbeddingService() {
        return embeddingServices.stream()
                .filter(es -> es.getName().equals(properties.getDefaultEmbeddingModel()))
                .findFirst()
                .orElseThrow(() -> createDefaultEmbeddingServiceNotFoundException());
    }

    private IllegalArgumentException createDefaultEmbeddingServiceNotFoundException() {
        return new IllegalArgumentException(
                "Default embedding service '" + properties.getDefaultEmbeddingModel() + 
                "' not found in available models: " + getEmbeddingServiceNames());
    }

    private String showModel(LlmService<?> model) {
        Set<String> roles = getRolesForModel(model.getName());
        String maybeRoles = roles.isEmpty() ? "" : " - Roles: " + String.join(", ", roles);
        return "name: " + model.getName() + ", provider: " + model.getProvider() + maybeRoles;
    }

    private Set<String> getRolesForModel(String modelName) {
        return properties.getLlms().entrySet().stream()
                .filter(entry -> entry.getValue().equals(modelName))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    private String showEmbeddingModel(EmbeddingService model) {
        Set<String> roles = getRolesForEmbeddingModel(model.getName());
        String maybeRoles = roles.isEmpty() ? "" : " - Roles: " + String.join(", ", roles);
        return "name: " + model.getName() + ", provider: " + model.getProvider() + maybeRoles;
    }

    private Set<String> getRolesForEmbeddingModel(String modelName) {
        return properties.getEmbeddingServices().entrySet().stream()
                .filter(entry -> entry.getValue().equals(modelName))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    @Override
    public List<ModelMetadata> listModels() {
        List<ModelMetadata> models = new ArrayList<>();
        models.addAll(createLlmMetadataList());
        models.addAll(createEmbeddingServiceMetadataList());
        return models;
    }

    private List<ModelMetadata> createLlmMetadataList() {
        return llms.stream().map(this::createLlmMetadata).collect(Collectors.toList());
    }

    private LlmMetadata createLlmMetadata(LlmService<?> llm) {
        return LlmMetadata.create(
                llm.getName(), llm.getProvider(), 
                llm.getKnowledgeCutoffDate(), llm.getPricingModel());
    }

    private List<ModelMetadata> createEmbeddingServiceMetadataList() {
        return embeddingServices.stream().map(this::createEmbeddingServiceMetadata).collect(Collectors.toList());
    }

    private EmbeddingServiceMetadata createEmbeddingServiceMetadata(EmbeddingService es) {
        return EmbeddingServiceMetadata.create(es.getName(), es.getProvider(), es.getPricingModel());
    }

    @Override
    public String infoString(Boolean verbose, int indent) {
        String llmsInfo = buildLlmsInfo();
        String embeddingServicesInfo = buildEmbeddingServicesInfo();
        String result = buildFullInfoString(llmsInfo, embeddingServicesInfo);
        return indentString(result, indent);
    }

    private String buildLlmsInfo() {
        return "Available LLMs:\n\t" +
                llms.stream()
                        .sorted(Comparator.comparing(LlmService::getName))
                        .map(this::showModel)
                        .collect(Collectors.joining("\n\t"));
    }

    private String buildEmbeddingServicesInfo() {
        return "Available embedding services:\n\t" +
                embeddingServices.stream()
                        .sorted(Comparator.comparing(EmbeddingService::getName))
                        .map(this::showEmbeddingModel)
                        .collect(Collectors.joining("\n\t"));
    }

    private String buildFullInfoString(String llmsInfo, String embeddingServicesInfo) {
        return "Default LLM: " + properties.getDefaultLlm() + "\n" +
                llmsInfo + "\n" +
                "Default embedding service: " + properties.getDefaultEmbeddingModel() + "\n" +
                embeddingServicesInfo;
    }

    private String indentString(String str, int indent) {
        if (indent <= 0) return str;
        String indentation = " ".repeat(indent);
        return str.lines().map(line -> indentation + line).collect(Collectors.joining("\n"));
    }

    @Override
    public List<String> listRoles(Class<?> modelClass) {
        if (LlmService.class.isAssignableFrom(modelClass)) {
            return new ArrayList<>(properties.getLlms().keySet());
        } else if (EmbeddingService.class.isAssignableFrom(modelClass)) {
            return new ArrayList<>(properties.getEmbeddingServices().keySet());
        }
        throw new IllegalArgumentException("Unsupported model class: " + modelClass);
    }

    @Override
    public List<String> listModelNames(Class<?> modelClass) {
        if (LlmService.class.isAssignableFrom(modelClass)) {
            return getLlmNames();
        } else if (EmbeddingService.class.isAssignableFrom(modelClass)) {
            return getEmbeddingServiceNames();
        }
        throw new IllegalArgumentException("Unsupported model class: " + modelClass);
    }

    @Override
    public LlmService<?> getLlm(ModelSelectionCriteria criteria) {
        if (criteria instanceof ByRoleModelSelectionCriteria) {
            return getLlmByRole((ByRoleModelSelectionCriteria) criteria);
        } else if (criteria instanceof ByNameModelSelectionCriteria) {
            return getLlmByName((ByNameModelSelectionCriteria) criteria);
        } else if (criteria instanceof RandomByNameModelSelectionCriteria) {
            return getLlmRandom((RandomByNameModelSelectionCriteria) criteria);
        } else if (criteria instanceof FallbackByNameModelSelectionCriteria) {
            return getLlmFallback((FallbackByNameModelSelectionCriteria) criteria);
        } else if (criteria instanceof AutoModelSelectionCriteria) {
            throw new IllegalStateException("Auto model selection criteria should have been resolved upstream");
        } else if (criteria instanceof DefaultModelSelectionCriteria) {
            return defaultLlm;
        } else if (criteria instanceof PreResolvedModelSelectionCriteria) {
            return getLlmFromPreResolved((PreResolvedModelSelectionCriteria<?>) criteria);
        }
        throw new IllegalArgumentException("Unsupported criteria type: " + criteria.getClass());
    }

    private LlmService<?> getLlmByRole(ByRoleModelSelectionCriteria criteria) {
        String modelName = properties.getLlms().get(criteria.getRole());
        if (modelName == null) {
            throw new NoSuitableModelException(criteria, getLlmNames());
        }
        return findLlmByName(modelName, criteria);
    }

    private LlmService<?> findLlmByName(String modelName, ModelSelectionCriteria criteria) {
        return llms.stream()
                .filter(llm -> llm.getName().equals(modelName))
                .findFirst()
                .orElseThrow(() -> new NoSuitableModelException(criteria, getLlmNames()));
    }

    private LlmService<?> getLlmByName(ByNameModelSelectionCriteria criteria) {
        return llms.stream()
                .filter(llm -> llm.getName().equals(criteria.getName()))
                .findFirst()
                .orElseThrow(() -> new NoSuitableModelException(criteria, getLlmNames()));
    }

    private LlmService<?> getLlmRandom(RandomByNameModelSelectionCriteria criteria) {
        List<LlmService<?>> models = llms.stream()
                .filter(llm -> criteria.getNames().contains(llm.getName()))
                .collect(Collectors.toList());
        if (models.isEmpty()) {
            throw new NoSuitableModelException(criteria, getLlmNames());
        }
        return models.get(new Random().nextInt(models.size()));
    }

    private LlmService<?> getLlmFallback(FallbackByNameModelSelectionCriteria criteria) {
        LlmService<?> llm = null;
        for (String requestedName : criteria.getNames()) {
            llm = findLlmByNameOrNull(requestedName);
            if (llm != null) break;
            log.info("Requested LLM '{}' not found", requestedName);
        }
        if (llm != null) return llm;
        throw new NoSuitableModelException(criteria, getLlmNames());
    }

    private LlmService<?> findLlmByNameOrNull(String name) {
        return llms.stream()
                .filter(l -> name.equals(l.getName()))
                .findFirst()
                .orElse(null);
    }

    @SuppressWarnings("unchecked")
    private LlmService<?> getLlmFromPreResolved(PreResolvedModelSelectionCriteria<?> criteria) {
        return (LlmService<?>) criteria.getResolved();
    }

    @Override
    public EmbeddingService getEmbeddingService(ModelSelectionCriteria criteria) {
        if (criteria instanceof ByRoleModelSelectionCriteria) {
            return getEmbeddingServiceByRole((ByRoleModelSelectionCriteria) criteria);
        }
        return defaultEmbeddingService();
    }

    private EmbeddingService getEmbeddingServiceByRole(ByRoleModelSelectionCriteria criteria) {
        String modelName = properties.getEmbeddingServices().get(criteria.getRole());
        if (modelName == null) {
            throw NoSuitableModelException.forModels(criteria, embeddingServices);
        }
        return findEmbeddingServiceByName(modelName, criteria);
    }

    private EmbeddingService findEmbeddingServiceByName(String modelName, ModelSelectionCriteria criteria) {
        return embeddingServices.stream()
                .filter(es -> es.getName().equals(modelName))
                .findFirst()
                .orElseThrow(() -> NoSuitableModelException.forModels(criteria, embeddingServices));
    }
}
