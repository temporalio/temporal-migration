package io.temporal.examples.backend;

import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;

import java.time.Duration;

@WorkflowImpl(taskQueues = MigrateableWorkflowImpl.taskQueue)
public class MigrateableWorkflowImpl implements MigrateableWorkflow {
    public static final String taskQueue = "default";
    private MigrateableWorkflowParams params;

    @Override
    public MigrateableWorkflowResult execute(MigrateableWorkflowParams params) {
        MigrateableWorkflowResult result = new MigrateableWorkflowResult();
        this.params = params;
        result.setValue(params.getValue());
        Workflow.sleep(Duration.ofSeconds(params.getKeepAliveDurationSecs()));
        return result;
    }

    @Override
    public MigrateableWorkflowParams getMigrationState() {
        return this.params;
    }

    @Override
    public void setValue(String value) {
        this.params.setValue(value);
    }
}
