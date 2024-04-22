package io.temporal.migration.support;

public class MigrationState {
    private MigrateableWorkflowParams params;
    private ExecutionState executionState;

    public MigrateableWorkflowParams getParams() {
        return params;
    }

    public void setParams(MigrateableWorkflowParams params) {
        this.params = params;
    }

    public ExecutionState getExecutionState() {
        return executionState;
    }

    public void setExecutionState(ExecutionState executionState) {
        this.executionState = executionState;
    }
}
