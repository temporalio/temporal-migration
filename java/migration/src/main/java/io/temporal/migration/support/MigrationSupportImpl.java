package io.temporal.migration.support;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.temporal.activity.Activity;
import io.temporal.api.enums.v1.WorkflowExecutionStatus;
import io.temporal.api.workflowservice.v1.DescribeWorkflowExecutionRequest;
import io.temporal.api.workflowservice.v1.DescribeWorkflowExecutionResponse;
import io.temporal.api.workflowservice.v1.WorkflowServiceGrpc;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowNotFoundException;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;
import io.temporal.failure.ApplicationFailure;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.spring.boot.ActivityImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component("migration-support")
public class MigrationSupportImpl implements MigrationSupport {
    private static Logger logger = LoggerFactory.getLogger(MigrationSupportImpl.class);

    private  WorkflowClient legacyWorkflowClient;
    private WorkflowClient targetWorkflowClient;
    private  String migrationStateQueryName;

    public MigrationSupportImpl(Clients clients, MigrationProperties migrationProperties) {

        this.legacyWorkflowClient = clients.getLegacyClient();
        this.targetWorkflowClient = clients.getTargetClient();
        this.migrationStateQueryName = migrationProperties.getStateQueryName();
    }

    private void sleep(int seconds) {
        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(seconds));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    @Override
    public PullLegacyExecutionResponse pullLegacyExecutionInfo(PullLegacyExecutionRequest cmd) {

        if(cmd.getPollingDurationSecs() == 0) {
            cmd.setPollingDurationSecs(2);
        }
        if (cmd.getNamespace() == null || Objects.equals(cmd.getNamespace(), "")) {
            cmd.setNamespace(this.legacyWorkflowClient.getOptions().getNamespace());
        }
        if (Objects.equals(cmd.getWorkflowId(), "")) {
            cmd.setWorkflowId(Activity.getExecutionContext().getInfo().getWorkflowId());
        }

        PullLegacyExecutionResponse resp = new PullLegacyExecutionResponse();
        WorkflowStub legacyWF = this.legacyWorkflowClient.newUntypedWorkflowStub(cmd.getWorkflowId());
        WorkflowServiceStubs svc = this.legacyWorkflowClient.getWorkflowServiceStubs();
        WorkflowServiceGrpc.WorkflowServiceBlockingStub stub = svc.blockingStub();
        logger.info("pulling migrationState from legacy namespace {}", cmd.getNamespace());
        DescribeWorkflowExecutionRequest req = DescribeWorkflowExecutionRequest.newBuilder().
                setNamespace(cmd.getNamespace()).
                setExecution(legacyWF.getExecution()).build();

        while (true) {
            try {
                Activity.getExecutionContext().heartbeat(resp.getElapsedTime());
                DescribeWorkflowExecutionResponse wfResp = stub.describeWorkflowExecution(req);
                WorkflowExecutionStatus stat = wfResp.getWorkflowExecutionInfo().getStatus();
                logger.info("received legacy execution status for {}/{}/{} = {}", cmd.getNamespace(), cmd.getWorkflowType(),cmd.getWorkflowId(), stat);
                if (Objects.equals(wfResp.getWorkflowExecutionInfo().getStatus(),WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_COMPLETED)) {
                    Object out = legacyWF.query(migrationStateQueryName, Object.class);
                    resp.setMigrationState(out);
                    resp.setStatus(stat);
                    resp.setResumable(true);
                    return resp;
                }
                sleep(cmd.getPollingDurationSecs());
                resp.setElapsedTime(resp.getElapsedTime() + cmd.getPollingDurationSecs());

            } catch (WorkflowNotFoundException e) {
                throw ApplicationFailure.newNonRetryableFailure("workflow execution not found", "not found", e.getMessage());
            } catch (StatusRuntimeException e) {
                if (Objects.equals(e.getStatus().getCode(),Status.Code.NOT_FOUND)) {
                    throw ApplicationFailure.newNonRetryableFailure("workflow execution not found", "not found", e.getMessage());
                }
            }
        }
    }

    @Override
    public PushTargetExecutionResponse pushToTargetExecution(PushTargetExecutionRequest cmd) {

        // check that execution in target exists
        WorkflowStub targetWF = this.
                targetWorkflowClient.
                newUntypedWorkflowStub(cmd.getWorkflowId());
        WorkflowServiceStubs svc = this.targetWorkflowClient.getWorkflowServiceStubs();
        WorkflowServiceGrpc.WorkflowServiceBlockingStub stub = svc.blockingStub();
        DescribeWorkflowExecutionRequest req = DescribeWorkflowExecutionRequest.newBuilder().
                setNamespace(this.targetWorkflowClient.getOptions().getNamespace()).
                setExecution(targetWF.getExecution()).build();
        PushTargetExecutionResponse resp = new PushTargetExecutionResponse();
        try {
            DescribeWorkflowExecutionResponse ignore = stub.describeWorkflowExecution(req);
        } catch(WorkflowNotFoundException e) {
            logger.info("workflow {} not found.starting in target", cmd.getWorkflowId());
            return resumeInTarget(cmd, resp);
        } catch(StatusRuntimeException e) {
            if(Objects.equals(e.getStatus().getCode(),Status.Code.NOT_FOUND)) {
                logger.info("workflow {} not found.starting in target", cmd.getWorkflowId());
                return resumeInTarget(cmd, resp);
            }
            throw e;
        } catch(Exception e) {
            logger.error("failed to resumeInTarget", e);
            throw e;
        }
        return resp;
    }

    private PushTargetExecutionResponse resumeInTarget(PushTargetExecutionRequest cmd, PushTargetExecutionResponse resp) {
        WorkflowStub workflow = this.targetWorkflowClient.newUntypedWorkflowStub(cmd.getWorkflowType(),
                WorkflowOptions.newBuilder().
                        setWorkflowId(cmd.getWorkflowId()).
                        setTaskQueue(cmd.getTaskQueue()).
                        build());
        workflow.start(cmd.getArguments());
        resp.setStarted(true);
        return resp;
    }
}
