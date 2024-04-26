package io.temporal.migration.support;

import io.temporal.client.WorkflowClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ClientsImpl implements Clients {
    @Override
    public WorkflowClient getLegacyClient() {
        return legacyClient;
    }

    @Override
    public WorkflowClient getTargetClient() {
        return targetClient;
    }

    private WorkflowClient legacyClient;
    private WorkflowClient targetClient;
    public ClientsImpl(
            @Value("${spring.temporal.connection.target}") String temporalTarget,
            @Qualifier("temporalWorkflowClient") WorkflowClient legacyClient,
            @Qualifier("targetTemporalWorkflowClient") WorkflowClient targetClient
    ) {
        this.legacyClient = legacyClient;
        this.targetClient = targetClient;
    }

}
