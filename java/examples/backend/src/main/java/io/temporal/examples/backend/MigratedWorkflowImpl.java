package io.temporal.examples.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.temporal.activity.ActivityOptions;
import io.temporal.failure.ApplicationFailure;
import io.temporal.migration.support.MigrationSupport;
import io.temporal.migration.support.PullLegacyExecutionRequest;
import io.temporal.migration.support.PullLegacyExecutionResponse;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowInfo;

import java.time.Duration;

@WorkflowImpl(taskQueues = MigratedWorkflowImpl.taskQueue)
public class MigratedWorkflowImpl implements MigrateableWorkflow {
    public static final String taskQueue = "default";
    private final MigrationSupport acts;
    private MigrateableWorkflowParams params;

    public MigratedWorkflowImpl() {
        this.acts = Workflow.newActivityStub(
                MigrationSupport.class,
                ActivityOptions.
                        newBuilder().
                        setStartToCloseTimeout(Duration.ofSeconds(10)).
                        build());
    }

    @Override
    public MigrateableWorkflowResult execute(MigrateableWorkflowParams params) {
        this.params = params;
        MigrateableWorkflowResult result = new MigrateableWorkflowResult();
        result.setValue(params.getValue());

        // here we poll right away to get legacy execution state
        if(params.getExecutionState() == null || !params.getExecutionState().isMigrated()) {
            WorkflowInfo info = Workflow.getInfo();
            PullLegacyExecutionResponse pullLegacyExecutionResponse = this.acts.pullLegacyExecutionInfo(new PullLegacyExecutionRequest(
                    info.getNamespace(),
                    info.getWorkflowType(),
                    info.getWorkflowId(),
                    2
            ));
            if(!pullLegacyExecutionResponse.isResumable()) {
                // what if we could not poll for the value?
                throw ApplicationFailure.newFailure("could not migrate", "migration_failure");
            }
            ObjectMapper mapper = new ObjectMapper();
            MigrateableWorkflowParams migratedParams = mapper.convertValue(pullLegacyExecutionResponse.getMigrationState(), MigrateableWorkflowParams.class);
            MigrateableWorkflow stub = Workflow.newContinueAsNewStub(MigrateableWorkflow.class);
            return stub.execute(migratedParams);
        }

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
