package io.temporal.examples.frontend;

import io.temporal.client.WorkflowClient;

public interface Clients {
    WorkflowClient getLegacyClient();

    WorkflowClient getTargetClient();
}
