package io.temporal.examples.backend;

import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface MigrateableWorkflow {
    @WorkflowMethod
    MigrateableWorkflowResult execute(MigrateableWorkflowParams params);

    @QueryMethod
    MigrateableWorkflowParams getMigrationState();

    @QueryMethod
    MigrateableWorkflowResult getCurrentResult();

    @SignalMethod
    void setValue(String value);
}
