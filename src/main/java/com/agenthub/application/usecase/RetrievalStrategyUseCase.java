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
        applyCommandFields(retrievalStrategy, command);
        return repository.save(retrievalStrategy);
    }

    private void applyCommandFields(RetrievalStrategy target, UpdateRetrievalStrategyCommand command) {
        applyStringFields(target, command);
        applyNumericFields(target, command);
        applyToggleFields(target, command);
    }

    private void applyStringFields(RetrievalStrategy target, UpdateRetrievalStrategyCommand c) {
        if (c.getName() != null) target.setName(c.getName());
        if (c.getDescription() != null) target.setDescription(c.getDescription());
        if (c.getRerankModel() != null) target.setRerankModel(c.getRerankModel());
    }

    private void applyNumericFields(RetrievalStrategy target, UpdateRetrievalStrategyCommand c) {
        if (c.getTopK() != null) target.setTopK(c.getTopK());
        if (c.getScoreThreshold() != null) target.setScoreThreshold(c.getScoreThreshold());
        if (c.getVectorWeight() != null) target.setVectorWeight(c.getVectorWeight());
        if (c.getKeywordWeight() != null) target.setKeywordWeight(c.getKeywordWeight());
    }

    private void applyToggleFields(RetrievalStrategy target, UpdateRetrievalStrategyCommand c) {
        if (c.getEnableRerank() != null) target.setEnableRerank(c.getEnableRerank());
        if (c.getEnableQueryRewrite() != null) target.setEnableQueryRewrite(c.getEnableQueryRewrite());
        if (c.getEnableTextSearch() != null) target.setEnableTextSearch(c.getEnableTextSearch());
        if (c.getEnableVectorSearch() != null) target.setEnableVectorSearch(c.getEnableVectorSearch());
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
