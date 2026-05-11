package com.agenthub.infrastructure.agents.ali.saver;

import com.alibaba.cloud.ai.graph.checkpoint.BaseCheckpointSaver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * @author huangdayu
 */
@RequiredArgsConstructor
@Component
public class SaverFactory {
    private final DataSource dataSource;

    public BaseCheckpointSaver postgresSaver() {
        return PostgresSaver.builder().datasource(dataSource).createTables(true).dropTablesFirst(false).build();
    }

}
