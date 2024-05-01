package io.temporal.migration.support;

import io.temporal.spring.boot.WorkflowImpl;

@WorkflowImpl(taskQueues = MigratedWorkflowImpl.taskQueue)
public class MigratedWorkflowImpl implements MigrateableWorkflow{
    public static final String taskQueue = "target";
    private MigrateableWorkflowParams params;

    @Override
    public MigrateableWorkflowResult execute(MigrateableWorkflowParams params) {
        this.params = params;
        return new MigrateableWorkflowResult(params.getValue());
    }

    @Override
    public MigrationState getMigrationState() {
        MigrationState out = new MigrationState();
        out.setParams(this.params);
        out.setExecutionState(new ExecutionState(true));
        return out;
    }

    @Override
    public void mySimpleSignal() {

    }
}
