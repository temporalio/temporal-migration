package io.temporal.migration.support;

import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface MigrateableWorkflow {
    @WorkflowMethod
    MigrateableWorkflowResult execute(MigrateableWorkflowParams params);

    @QueryMethod
    MigrationState getMigrationState();
}
