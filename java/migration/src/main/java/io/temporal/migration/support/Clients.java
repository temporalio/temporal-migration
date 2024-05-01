package io.temporal.migration.support;

import io.temporal.client.WorkflowClient;

public interface Clients {
    WorkflowClient getLegacyClient();

    WorkflowClient getTargetClient();
}
