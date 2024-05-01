package io.temporal.examples.simulator;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.temporal.activity.Activity;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.api.enums.v1.WorkflowExecutionStatus;
import io.temporal.api.workflowservice.v1.DescribeWorkflowExecutionRequest;
import io.temporal.api.workflowservice.v1.DescribeWorkflowExecutionResponse;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowNotFoundException;
import io.temporal.examples.backend.MigrateableWorkflow;
import io.temporal.examples.common.Clients;
import io.temporal.failure.ApplicationFailure;
import io.temporal.serviceclient.WorkflowServiceStubs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Objects;

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
    public SignalDetailsResponse signalUntilClosed(SignalParams params) {
        WorkflowServiceStubs service = legacyNamespaceClient.getWorkflowServiceStubs();

        WorkflowExecutionStatus status = WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_RUNNING;
        SignalDetailsResponse response = new SignalDetailsResponse();
        while (Objects.equals(status,WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_RUNNING)) {
            WorkflowExecution execution = WorkflowExecution.newBuilder().setWorkflowId(params.getWorkflowId()).build();

            DescribeWorkflowExecutionResponse description = service.
                    blockingStub().
                    describeWorkflowExecution(DescribeWorkflowExecutionRequest.
                            newBuilder().
                            setExecution(execution).
                            setNamespace(legacyNamespaceClient.getOptions().getNamespace()).
                            build());
            status = description.getWorkflowExecutionInfo().getStatus();

            if (Objects.equals(status,WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_RUNNING)) {
                MigrateableWorkflow wf = this.legacyNamespaceClient.newWorkflowStub(MigrateableWorkflow.class, params.getWorkflowId());
                String value = Simulator.now();
                try {
                    wf.setValue(value);
                    response.markSuccessLegacy(value);
                } catch (ApplicationFailure e) {
                    logger.error(e.toString(), e);
                    return response;
                }
            }
            try {
                Thread.sleep(params.getSignalFrequencyMillis());
            } catch (InterruptedException e) {
                logger.error(e.toString(), e);
                return response;
            }
            Activity.getExecutionContext().heartbeat(null);
        }
        logger.info("workflowID existing '{}' with status '{}'", params, status);

        return response;
    }

    @Override
    public SignalDetailsResponse signalUntilAndAfterMigrated(SignalParams params) {
        if(params.getSignalTargetThresholdCount() == 0) {
            params.setSignalTargetThresholdCount(50);
        }
        boolean hasMigrated = false;
        boolean signalable = true;

        WorkflowExecutionStatus status = WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_RUNNING;
        SignalDetailsResponse response = new SignalDetailsResponse(params.getWorkflowId(), "UNDEFINED");

        while (signalable) {
            String value = Simulator.now();
            Activity.getExecutionContext().heartbeat(response);
            if (!hasMigrated) {
                try {
                    MigrateableWorkflow wf = this.legacyNamespaceClient.newWorkflowStub(MigrateableWorkflow.class, params.getWorkflowId());
                    wf.setValue(value);
                    response.markSuccessLegacy(value);
                    try {
                        Thread.sleep(params.getSignalFrequencyMillis());
                    } catch (InterruptedException e) {
                        logger.error(e.toString(),e);
                        return response;
                    }
                    continue;
                } catch (WorkflowNotFoundException e) {
                    response.markFailedLegacy(value);
                    logger.warn("workflow {} not found...forwarding in target", params, e);
                } catch (StatusRuntimeException e) {
                    response.markFailedLegacy(value);
                    if (Objects.equals(e.getStatus().getCode(),Status.Code.NOT_FOUND)) {
                        logger.debug("workflow {} not found...forwarding in target", params);
                        // swallow this error
                    }
                } catch (ApplicationFailure e) {
                    response.markFailedLegacy(value);
                    logger.error(e.toString(), e);
                }
            }
            try {
                MigrateableWorkflow wf = this.targetNamespaceClient.
                        newWorkflowStub(MigrateableWorkflow.class, params.getWorkflowId());
                // it is possible the target has not completed pulling the value from the legacy execution
                // so if we send this signal we either need to
                // 1) enqueue the signal in the target execution to avoid dropping it or
                // 2) inspect the `isMigrated` value from the target execution to see if it can receive the signal yet
                // -- enqueue the signal if it is not ready
                wf.setValue(value);
                response.markSuccessTarget(value);
                hasMigrated = true;

                try {
                    Thread.sleep(params.getSignalFrequencyMillis());
                } catch (InterruptedException e) {
                    logger.error("thread interrupt", e);
                    return response;
                }
            } catch(WorkflowNotFoundException e) {
                logger.warn("workflow {} not found in target", params);
                response.markFailedTarget(value);
                if(response.getFailedTargetSignalAttempts() > 10) {
                    signalable = false;
                }
            } catch (StatusRuntimeException e) {
                response.markFailedTarget(value);
                if (Objects.equals(e.getStatus().getCode(), Status.Code.NOT_FOUND)) {
                    logger.warn("workflow {} not found in target", params);
                    if(response.getFailedTargetSignalAttempts() > 10) {
                        signalable = false;
                    }
                }
            } catch (ApplicationFailure e) {
                response.markFailedTarget(value);
                signalable = false;
            }
            if(signalable) {
                signalable = (response.getFailedTargetSignalAttempts() + response.getSuccessTargetSignalAttempts()) < params.getSignalTargetThresholdCount();
            }
        }
        logger.info("workflowID existing '{}' with status '{}'", params, status);
        return response;
    }
}
