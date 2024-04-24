package io.temporal.examples.starter;

import io.temporal.client.WorkflowClient;

public interface Clients {
    WorkflowClient getLegacyClient();

    WorkflowClient getTargetClient();
}
