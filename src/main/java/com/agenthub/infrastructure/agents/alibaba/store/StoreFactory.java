package com.agenthub.infrastructure.agents.alibaba.store;

import com.alibaba.cloud.ai.graph.store.stores.DatabaseStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * @author huangdayu
 */
@Component
@RequiredArgsConstructor
public class StoreFactory {

    private final DataSource dataSource;

    public DatabaseStore databaseStore() {
        return new DatabaseStore(dataSource);
    }

}
