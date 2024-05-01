package io.temporal.examples.common;

import io.temporal.client.WorkflowClient;

public interface Clients {
    WorkflowClient getLegacyClient();

    WorkflowClient getTargetClient();
}
