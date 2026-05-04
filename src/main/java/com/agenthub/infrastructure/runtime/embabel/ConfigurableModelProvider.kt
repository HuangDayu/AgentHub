package com.agenthub.infrastructure.runtime.embabel

import com.embabel.agent.spi.LlmService
import com.embabel.common.ai.model.AutoModelSelectionCriteria
import com.embabel.common.ai.model.ByNameModelSelectionCriteria
import com.embabel.common.ai.model.ByRoleModelSelectionCriteria
import com.embabel.common.ai.model.ConfigurableModelProvider
import com.embabel.common.ai.model.ConfigurableModelProviderProperties
import com.embabel.common.ai.model.DefaultModelSelectionCriteria
import com.embabel.common.ai.model.EmbeddingService
import com.embabel.common.ai.model.EmbeddingServiceMetadata
import com.embabel.common.ai.model.EmbeddingServiceMetadata.Companion.invoke
import com.embabel.common.ai.model.FallbackByNameModelSelectionCriteria
import com.embabel.common.ai.model.LlmMetadata
import com.embabel.common.ai.model.LlmMetadata.Companion.invoke
import com.embabel.common.ai.model.ModelMetadata
import com.embabel.common.ai.model.ModelProvider
import com.embabel.common.ai.model.ModelSelectionCriteria
import com.embabel.common.ai.model.NoSuitableModelException
import com.embabel.common.ai.model.PreResolvedModelSelectionCriteria
import com.embabel.common.ai.model.RandomByNameModelSelectionCriteria
import com.embabel.common.util.indent
import com.embabel.common.util.loggerFor
import kotlin.collections.component1
import kotlin.collections.component2

/**
 * @author huangdayu
 */
class ConfigurableModelProvider(
    private val llms: List<LlmService<*>>,
    private val embeddingServices: List<EmbeddingService>,
    private val properties: ConfigurableModelProviderProperties,
) : ModelProvider {

    private val logger = loggerFor<ConfigurableModelProvider>()

    private val defaultLlm = llms.firstOrNull { it.name == properties.defaultLlm }
        ?: throw IllegalArgumentException("Default LLM '${properties.defaultLlm}' not found in available models: ${llms.map { it.name }}")

    // Compute this lazily as embedding services may not be available
    private fun defaultEmbeddingService() =
        embeddingServices.firstOrNull { it.name == properties.defaultEmbeddingModel }
            ?: throw IllegalArgumentException("Default embedding service '${properties.defaultEmbeddingModel}' not found in available models: ${embeddingServices.map { it.name }}")

    init {
        properties.llms.forEach { (role, model) ->
            if (llms.none { it.name == model }) {
                error("LLM '$model' for role $role is not available: Choices are ${llms.map { it.name }}")
            }
        }
        logger.info(infoString(verbose = true))

        properties.embeddingServices.forEach { (role, model) ->
            if (embeddingServices.none { it.name == model }) {
                error("Embedding model '$model' for role $role is not available: Choices are ${embeddingServices.map { it.name }}")
            }
        }
    }

    private fun showModel(model: LlmService<*>): String {
        val roles = properties.llms.filter { it.value == model.name }.keys
        val maybeRoles = if (roles.isNotEmpty()) " - Roles: ${roles.joinToString(", ")}" else ""
        return "name: ${model.name}, provider: ${model.provider}$maybeRoles"
    }

    private fun showEmbeddingModel(model: EmbeddingService): String {
        val roles = properties.embeddingServices.filter { it.value == model.name }.keys
        val maybeRoles = if (roles.isNotEmpty()) " - Roles: ${roles.joinToString(", ")}" else ""
        return "name: ${model.name}, provider: ${model.provider}$maybeRoles"
    }

    override fun listModels(): List<ModelMetadata> =
        llms.map {
            LlmMetadata(
                it.name,
                provider = it.provider,
                knowledgeCutoffDate = it.knowledgeCutoffDate,
                pricingModel = it.pricingModel,
            )
        } + embeddingServices.map {
            EmbeddingServiceMetadata(
                it.name,
                provider = it.provider,
                pricingModel = it.pricingModel,
            )
        }


    override fun infoString(
        verbose: Boolean?,
        indent: Int,
    ): String {
        val llmsInfo = "Available LLMs:\n\t${
            llms
                .sortedBy { it.name }
                .joinToString("\n\t") { showModel(it) }
        }"
        val embeddingServicesInfo =
            "Available embedding services:\n\t${
                embeddingServices
                    .sortedBy { it.name }
                    .joinToString("\n\t") { showEmbeddingModel(it) }
            }"
        return "Default LLM: ${properties.defaultLlm}\n$llmsInfo\nDefault embedding service: ${properties.defaultEmbeddingModel}\n$embeddingServicesInfo".indent(
            indent
        )
    }

    override fun listRoles(modelClass: Class<*>): List<String> {
        return when {
            LlmService::class.java.isAssignableFrom(modelClass) -> properties.llms.keys.toList()
            EmbeddingService::class.java.isAssignableFrom(modelClass) -> properties.embeddingServices.keys.toList()
            else -> throw IllegalArgumentException("Unsupported model class: $modelClass")
        }
    }

    override fun listModelNames(modelClass: Class<*>): List<String> {
        return when {
            LlmService::class.java.isAssignableFrom(modelClass) -> llms.map { it.name }
            EmbeddingService::class.java.isAssignableFrom(modelClass) -> embeddingServices.map { it.name }
            else -> throw IllegalArgumentException("Unsupported model class: $modelClass")
        }
    }

    override fun getLlm(criteria: ModelSelectionCriteria): LlmService<*> =
        when (criteria) {
            is ByRoleModelSelectionCriteria -> {
                val modelName = properties.llms[criteria.role] ?: throw NoSuitableModelException(criteria, llms.map { it.name })
                llms.firstOrNull { it.name == modelName } ?: throw NoSuitableModelException(criteria, llms.map { it.name })
            }

            is ByNameModelSelectionCriteria -> {
                llms.firstOrNull { it.name == criteria.name } ?: throw NoSuitableModelException(criteria, llms.map { it.name })
            }

            is RandomByNameModelSelectionCriteria -> {
                val models = llms.filter { criteria.names.contains(it.name) }
                if (models.isEmpty()) {
                    throw NoSuitableModelException(criteria, llms.map { it.name })
                }
                models.random()
            }

            is FallbackByNameModelSelectionCriteria -> {
                var llm: LlmService<*>? = null
                for (requestedName in criteria.names) {
                    llm = llms.firstOrNull { requestedName == it.name }
                    if (llm != null) {
                        break
                    } else {
                        logger.info("Requested LLM '{}' not found", requestedName)
                    }
                }
                llm
                    ?: throw NoSuitableModelException(criteria, llms.map { it.name })
            }

            is AutoModelSelectionCriteria -> {
                // The infrastructure above this class should have resolved this
                error("Auto model selection criteria should have been resolved upstream")
            }

            is DefaultModelSelectionCriteria -> {
                defaultLlm
            }

            is PreResolvedModelSelectionCriteria<*> -> {
                @Suppress("UNCHECKED_CAST")
                criteria.resolved as LlmService<*>
            }
        }

    override fun getEmbeddingService(criteria: ModelSelectionCriteria): EmbeddingService =
        when (criteria) {
            is ByRoleModelSelectionCriteria -> {
                val modelName =
                    properties.embeddingServices[criteria.role] ?: throw NoSuitableModelException.forModels(
                        criteria,
                        embeddingServices,
                    )
                embeddingServices.firstOrNull { it.name == modelName } ?: throw NoSuitableModelException.forModels(
                    criteria,
                    embeddingServices,
                )
            }

            // TODO should handle other criteria
            else -> {
                defaultEmbeddingService()
            }
        }
}