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
import io.temporal.examples.common.Clients;
import io.temporal.failure.ApplicationFailure;
import io.temporal.serviceclient.WorkflowServiceStubs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("signal-activities")
public class SignalActivitiesImpl implements SignalActivities {
    private final WorkflowClient legacyNamespaceClient;
    private final WorkflowClient targetNamespaceClient;
    private final Logger logger = LoggerFactory.getLogger(SignalActivitiesImpl.class);

    public SignalActivitiesImpl(Clients clients) {
        this.legacyNamespaceClient = clients.getLegacyClient();
        this.targetNamespaceClient = clients.getTargetClient();
    }

    @Override
    public LastValue signalUntilClosed(SignalParams params) {
        WorkflowServiceStubs service = legacyNamespaceClient.getWorkflowServiceStubs();

        WorkflowExecutionStatus status = WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_RUNNING;
        LastValue last = new LastValue(params.getWorkflowId(), "UNDEFINED");
        while (status == WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_RUNNING) {
            WorkflowExecution execution = WorkflowExecution.newBuilder().setWorkflowId(params.getWorkflowId()).build();

            DescribeWorkflowExecutionResponse description = service.
                    blockingStub().
                    describeWorkflowExecution(DescribeWorkflowExecutionRequest.
                            newBuilder().
                            setExecution(execution).
                            setNamespace(legacyNamespaceClient.getOptions().getNamespace()).
                            build());
            status = description.getWorkflowExecutionInfo().getStatus();

            if (status == WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_RUNNING) {
                MigrateableWorkflow wf = this.legacyNamespaceClient.newWorkflowStub(MigrateableWorkflow.class, params.getWorkflowId());

                String value = Simulator.now();
                try {
                    wf.setValue(value);
                } catch (ApplicationFailure e) {
                    System.console().printf(e.toString());
                    return last;
                }
                last = new LastValue(params.getWorkflowId(), value);
            }
            try {
                Thread.sleep(params.getSignalFrequencyMillis());
            } catch (InterruptedException e) {
                System.console().printf(e.toString());
                return last;
            }
            Activity.getExecutionContext().heartbeat(null);
        }
        logger.info("workflowID existing '{}' with status '{}'", params, status);

        return last;
    }

    @Override
    public LastValue signalUntilAndAfterMigrated(SignalParams params) {
        boolean hasMigrated = false;
        boolean signalable = true;

        WorkflowExecutionStatus status = WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_RUNNING;
        LastValue last = new LastValue(params.getWorkflowId(), "UNDEFINED");
        while (signalable) {
            String value = Simulator.now();
            Activity.getExecutionContext().heartbeat(null);
            if (!hasMigrated) {
                try {
                    MigrateableWorkflow wf = this.legacyNamespaceClient.newWorkflowStub(MigrateableWorkflow.class, params.getWorkflowId());
                    wf.setValue(value);
                    last = new LastValue(params.getWorkflowId(), value);
                    try {
                        Thread.sleep(params.getSignalFrequencyMillis());
                    } catch (InterruptedException e) {
                        System.console().printf(e.toString());
                        return last;
                    }
                    continue;
                } catch (StatusRuntimeException e) {
                    if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
                        logger.debug("workflow {} not found...forwarding in target", params);
                        // swallow this error
                    }
                } catch (ApplicationFailure e) {
                    System.console().printf(e.toString());
                }
            }
            try {
                MigrateableWorkflow wf = this.targetNamespaceClient.newWorkflowStub(MigrateableWorkflow.class, params.getWorkflowId());
                wf.setValue(value);
                hasMigrated = true;
                last = new LastValue(params.getWorkflowId(), value);
                try {
                    Thread.sleep(params.getSignalFrequencyMillis());
                } catch (InterruptedException e) {
                    System.console().printf(e.toString());
                    return last;
                }
            } catch (StatusRuntimeException e) {
                if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
                    logger.debug("workflow {} not found...forwarding in target", params);
                    // swallow this error
                    signalable = false;
                }
            } catch (ApplicationFailure e) {
                signalable = false;
            }
        }
        // puT BACK IN THE THREAD SLEEP
        logger.info("workflowID existing '{}' with status '{}'", params, status);
        return last;
    }
}
