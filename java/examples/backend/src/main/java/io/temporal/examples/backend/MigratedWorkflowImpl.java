package io.temporal.examples.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.temporal.activity.ActivityOptions;
import io.temporal.failure.ApplicationFailure;
import io.temporal.migration.support.MigrationSupport;
import io.temporal.migration.support.PullLegacyExecutionRequest;
import io.temporal.migration.support.PullLegacyExecutionResponse;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowInfo;

import java.time.Duration;

public class MigratedWorkflowImpl implements MigrateableWorkflow {
    private final MigrationSupport acts;

    private final MigrateableWorkflowResult result ;

    public MigratedWorkflowImpl() {
        this.result = new MigrateableWorkflowResult();

        this.acts = Workflow.newActivityStub(
                MigrationSupport.class,
                ActivityOptions.
                        newBuilder().
                        setStartToCloseTimeout(Duration.ofMinutes(10)).
                        setHeartbeatTimeout(Duration.ofSeconds(10)).
                        build());
    }

    @Override
    public MigrateableWorkflowResult execute(MigrateableWorkflowParams params) {
        this.result.setParams(params);

        // here we poll right away to get legacy execution state
        // but note that signals could come in while the activity is pulling legacy state
        if(params.getExecutionState() == null || !params.getExecutionState().isMigrated()) {
            WorkflowInfo info = Workflow.getInfo();
            PullLegacyExecutionResponse pullLegacyExecutionResponse = this.acts.pullLegacyExecutionInfo(new PullLegacyExecutionRequest(
                    null,
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
            // executionState could be as complicated as we need.
            // here we are just flipping a 'isMigrated' flag to skip the poll after CAN
            if(migratedParams.getExecutionState() == null) {
                migratedParams.setExecutionState(new ExecutionState(true));
            } else {
                migratedParams.getExecutionState().setMigrated(true);
            }
            // store the migration state here
            // this is merging state changes we have received while pulling from legacy in prep for our CAN
            migratedParams.appendValue(this.result.getReceivedValues().toArray(new String[0]));
            MigrateableWorkflow stub = Workflow.newContinueAsNewStub(MigrateableWorkflow.class);
            return stub.execute(migratedParams);
        }

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
