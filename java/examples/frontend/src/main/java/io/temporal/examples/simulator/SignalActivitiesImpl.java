package io.temporal.examples.simulator;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.temporal.activity.Activity;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.api.enums.v1.WorkflowExecutionStatus;
import io.temporal.api.workflowservice.v1.DescribeWorkflowExecutionRequest;
import io.temporal.api.workflowservice.v1.DescribeWorkflowExecutionResponse;
import io.temporal.client.WorkflowClient;
import io.temporal.examples.backend.MigrateableWorkflow;
import io.temporal.failure.ApplicationFailure;
import io.temporal.serviceclient.WorkflowServiceStubs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SignalActivitiesImpl implements SignalActivities {
    private final WorkflowClient legacyNamespaceClient;
    private final WorkflowClient targetNamespaceClient;
    private final Logger logger = LoggerFactory.getLogger(SignalActivitiesImpl.class);

    public SignalActivitiesImpl(WorkflowClient legacyNamespaceClient, WorkflowClient targetNamespaceClient) {
        this.legacyNamespaceClient = legacyNamespaceClient;
        this.targetNamespaceClient = targetNamespaceClient;
    }

    @Override
    public LastValue signalUntilClosed(String workflowID) {
        WorkflowServiceStubs service = legacyNamespaceClient.getWorkflowServiceStubs();

        WorkflowExecutionStatus status = WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_RUNNING;
        LastValue last = new LastValue(workflowID, "UNDEFINED");
        while (status == WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_RUNNING) {
            WorkflowExecution execution = WorkflowExecution.newBuilder().setWorkflowId(workflowID).build();

            DescribeWorkflowExecutionResponse description = service.
                    blockingStub().
                    describeWorkflowExecution(DescribeWorkflowExecutionRequest.
                            newBuilder().
                            setExecution(execution).
                            setNamespace("default").
                            build());
            status = description.getWorkflowExecutionInfo().getStatus();

            if (status == WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_RUNNING) {
                MigrateableWorkflow wf = this.legacyNamespaceClient.newWorkflowStub(MigrateableWorkflow.class, workflowID);

                String value = Simulator.now();
                try {
                    wf.setValue(value);
                } catch (ApplicationFailure e) {
                    System.console().printf(e.toString());
                    return last;
                }
                last = new LastValue(workflowID, value);
            }
            try {
                Thread.sleep(AppConfig.SIGNAL_FREQUENCY_MILLIS);
            } catch (InterruptedException e) {
                System.console().printf(e.toString());
                return last;
            }
            Activity.getExecutionContext().heartbeat(null);
        }
        System.out.println(String.format("workflowID existing '%s' with status '%s'", workflowID, status));
        return last;
    }

    @Override
    public LastValue signalUntilAndAfterMigrated(String workflowID) {
        boolean hasMigrated = false;
        boolean signalable = true;

        WorkflowExecutionStatus status = WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_RUNNING;
        LastValue last = new LastValue(workflowID, "UNDEFINED");
        while (signalable) {
            String value = Simulator.now();
            Activity.getExecutionContext().heartbeat(null);
            if (!hasMigrated) {
                try {
                    LongRunningWorkflow wf = this.legacyNamespaceClient.newWorkflowStub(LongRunningWorkflow.class, workflowID);
                    wf.callThat(value);
                    last = new LastValue(workflowID, value);
                    continue;
                } catch (StatusRuntimeException e) {
                    if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
                        logger.debug("workflow {} not found...forwarding in target", workflowID);
                        // swallow this error
                    }
                } catch (ApplicationFailure e) {
                    System.console().printf(e.toString());
                }
            }
            try {
                LongRunningWorkflow wf = this.targetNamespaceClient.newWorkflowStub(LongRunningWorkflow.class, workflowID);
                wf.callThat(value);
                hasMigrated = true;
                last = new LastValue(workflowID, value);
            } catch (StatusRuntimeException e) {
                if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
                    logger.debug("workflow {} not found...forwarding in target", workflowID);
                    // swallow this error
                    signalable = false;
                }
            } catch (ApplicationFailure e) {
                signalable = false;
            }
        }

        System.out.println(String.format("workflowID existing '%s' with status '%s'", workflowID, status));
        return last;
    }
}
