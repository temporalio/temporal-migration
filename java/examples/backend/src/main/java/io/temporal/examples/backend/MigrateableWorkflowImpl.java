package io.temporal.examples.backend;

import io.temporal.workflow.Workflow;

import java.time.Duration;

public class MigrateableWorkflowImpl implements MigrateableWorkflow {
    private final MigrateableWorkflowResult result;

    public MigrateableWorkflowImpl() {
        this.result = new MigrateableWorkflowResult();
    }

    @Override
    public MigrateableWorkflowResult execute(MigrateableWorkflowParams params) {
        this.result.setParams(params);
        Workflow.sleep(Duration.ofSeconds(params.getKeepAliveDurationSecs()));
        return result;
    }

    @Override
    public MigrateableWorkflowParams getMigrationState() {
        return this.result.getParams();
    }

    @Override
    public MigrateableWorkflowResult getCurrentResult() {
        return this.result;
    }

    @Override
    public void setValue(String value) {
        this.result.appendValue(value);
    }
}
