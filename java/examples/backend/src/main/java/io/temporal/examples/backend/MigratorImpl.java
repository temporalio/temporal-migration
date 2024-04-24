package io.temporal.examples.backend;

import io.temporal.activity.LocalActivityOptions;
import io.temporal.migration.interceptor.Migrator;
import io.temporal.migration.support.MigrationSupport;
import io.temporal.migration.support.PushTargetExecutionRequest;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowInfo;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Objects;

@Component
public class MigratorImpl implements Migrator {
    @Override
    public void migrate(PushTargetExecutionRequest cmd) {
        // how you handle this migration is up to you
        // but any usage of the MigrationSupport activities during this
        // phase of the interceptor handler
        // should be done inside a LOCALActivity, not a reg'lar one.
        MigrationSupport stub = Workflow.newLocalActivityStub(
                MigrationSupport.class,
                LocalActivityOptions.newBuilder().
                        setStartToCloseTimeout(Duration.ofSeconds(5)).
                        build());
        stub.pushToTargetExecution(cmd);
    }

    @Override
    public boolean isMigrateable(WorkflowInfo info) {
        return Objects.equals(info.getWorkflowType(), "MigrateableWorkflow");
    }
}
