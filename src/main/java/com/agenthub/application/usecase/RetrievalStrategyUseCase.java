package com.agenthub.application.usecase;

import com.agenthub.common.exception.NotFoundException;
import com.agenthub.application.command.CreateRetrievalStrategyCommand;
import com.agenthub.application.command.UpdateRetrievalStrategyCommand;
import com.agenthub.application.port.out.repositories.RetrievalStrategyRepository;
import com.agenthub.domain.model.RetrievalStrategy;
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
        RetrievalStrategy strategy = get(id);
        strategy.updateBasicInfo(command.name(), command.description());
        return repository.save(strategy);
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
        RetrievalStrategy strategy = createStrategy(command);
        return repository.save(strategy);
    }

    private RetrievalStrategy createStrategy(CreateRetrievalStrategyCommand cmd) {
        RetrievalStrategy strategy = RetrievalStrategy.create(cmd.workspaceId(), cmd.name());
        strategy.updateBasicInfo(cmd.name(), cmd.description());
        strategy.configureRetrieval(RetrievalStrategy.RetrievalType.valueOf(cmd.retrievalType()), cmd.topK(), cmd.scoreThreshold());
        strategy.setWeights(cmd.vectorWeight(), cmd.keywordWeight());
        strategy.setEnableRerank(cmd.enableRerank());
        strategy.setRerankModel(cmd.rerankModel());
        strategy.setEnableQueryRewrite(cmd.enableQueryRewrite());
        strategy.setEnableTextSearch(cmd.enableTextSearch());
        strategy.setEnableVectorSearch(cmd.enableVectorSearch());
        return strategy;
    }




}
