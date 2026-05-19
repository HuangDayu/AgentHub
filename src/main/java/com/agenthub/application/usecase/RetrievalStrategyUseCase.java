package com.agenthub.application.usecase;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.domain.exception.NotFoundException;
import com.agenthub.application.command.CreateRetrievalStrategyCommand;
import com.agenthub.application.command.UpdateRetrievalStrategyCommand;
import com.agenthub.application.port.out.repositories.RetrievalStrategyRepository;
import com.agenthub.domain.model.strategy.RetrievalStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author huangdayu
 */
@Component
@RequiredArgsConstructor
public class RetrievalStrategyUseCase {

    private final RetrievalStrategyRepository repository;


    public RetrievalStrategy update(String id, UpdateRetrievalStrategyCommand command) {
        RetrievalStrategy retrievalStrategy = get(id);
        if (command.getName() != null) retrievalStrategy.setName(command.getName());
        if (command.getDescription() != null) retrievalStrategy.setDescription(command.getDescription());
        if (command.getTopK() != null) retrievalStrategy.setTopK(command.getTopK());
        if (command.getScoreThreshold() != null) retrievalStrategy.setScoreThreshold(command.getScoreThreshold());
        if (command.getEnableRerank() != null) retrievalStrategy.setEnableRerank(command.getEnableRerank());
        if (command.getEnableQueryRewrite() != null) retrievalStrategy.setEnableQueryRewrite(command.getEnableQueryRewrite());
        if (command.getEnableTextSearch() != null) retrievalStrategy.setEnableTextSearch(command.getEnableTextSearch());
        if (command.getEnableVectorSearch() != null) retrievalStrategy.setEnableVectorSearch(command.getEnableVectorSearch());
        if (command.getRerankModel() != null) retrievalStrategy.setRerankModel(command.getRerankModel());
        if (command.getVectorWeight() != null) retrievalStrategy.setVectorWeight(command.getVectorWeight());
        if (command.getKeywordWeight() != null) retrievalStrategy.setKeywordWeight(command.getKeywordWeight());
        return repository.save(retrievalStrategy);
    }


    public List<RetrievalStrategy> list(String workspaceId) {
        return repository.findByWorkspace(workspaceId);
    }


    public RetrievalStrategy get(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("RetrievalStrategy not found: " + id));
    }


    public void delete(String id) {
        get(id);
        repository.deleteById(id);
    }


    public RetrievalStrategy create(CreateRetrievalStrategyCommand command) {
        RetrievalStrategy strategy = BeanUtil.copyProperties(command, RetrievalStrategy.class);
        return repository.save(strategy);
    }




}
