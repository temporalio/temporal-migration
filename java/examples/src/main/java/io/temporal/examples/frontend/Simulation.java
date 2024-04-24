//package io.temporal.examples.frontend;
//
//import io.grpc.Status;
//import io.grpc.StatusRuntimeException;
//import io.temporal.activity.Activity;
//import io.temporal.activity.ActivityInterface;
//import io.temporal.activity.ActivityMethod;
//import io.temporal.activity.ActivityOptions;
//import io.temporal.api.common.v1.WorkflowExecution;
//import io.temporal.api.enums.v1.WorkflowExecutionStatus;
//import io.temporal.api.workflow.v1.WorkflowExecutionInfo;
//import io.temporal.api.workflowservice.v1.DescribeWorkflowExecutionRequest;
//import io.temporal.api.workflowservice.v1.DescribeWorkflowExecutionResponse;
//import io.temporal.api.workflowservice.v1.ListWorkflowExecutionsRequest;
//import io.temporal.api.workflowservice.v1.ListWorkflowExecutionsResponse;
//import io.temporal.client.WorkflowClient;
//import io.temporal.failure.ApplicationFailure;
//import io.temporal.serviceclient.WorkflowServiceStubs;
//import io.temporal.workflow.*;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.text.SimpleDateFormat;
//import java.time.Duration;
//import java.util.*;
//
//public class Simulation {
//    public static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss.SSS";
//
//    public static String now() {
//        Calendar cal = Calendar.getInstance();
//        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
//        return sdf.format(cal.getTime());
//    }
//    static final class LastValue {
//        private String workflowId;
//        private String value;
//        public LastValue(String workflowId, String value) {
//            this.value = value;
//            this.workflowId = workflowId;
//        }
//        public LastValue() {}
//
//        public String getWorkflowId() {
//            return workflowId;
//        }
//
//        public String getValue() {
//            return value;
//        }
//    }
//    public static class SimulationWorkflowParams {
//        private boolean failover;
//
//        public boolean isFailover() {
//            return failover;
//        }
//
//        public void setFailover(boolean failover) {
//            this.failover = failover;
//        }
//
//    }
//    @WorkflowInterface
//    public interface SimulationWorkflow{
//        @WorkflowMethod
//        void simulate(SimulationWorkflowParams params);
//
//        @QueryMethod
//        String getLastValue(String workflowId);
//    }
//
//    @ActivityInterface
//    public interface ExecutionActivities {
//        @ActivityMethod
//        List<String> getWorkflowIDs();
//    }
//
//    public static class ExecutionActivitiesImpl implements ExecutionActivities {
//        private final WorkflowClient legacyNamespaceClient;
//
//        public ExecutionActivitiesImpl(WorkflowClient legacyNamespaceClient) {
//            this.legacyNamespaceClient = legacyNamespaceClient;
//        }
//
//        @Override
//        public List<String> getWorkflowIDs() {
//            WorkflowServiceStubs service = legacyNamespaceClient.getWorkflowServiceStubs();
//            // 1. load workflowIDs to consider
//            // 2. send `callGiraffe` signal every 200ms to each workflow in the list if the workflow exists in legacy namespace
//            // 3. exit when all workflows no longer appear in legacy namespace
//            ListWorkflowExecutionsRequest listRequest = ListWorkflowExecutionsRequest.newBuilder().
//                    setNamespace("default").
//                    setQuery("WorkflowType='LongRunningWorkflow' AND ExecutionStatus='Running'").
//                    build();
//            ListWorkflowExecutionsResponse listResponse = service.blockingStub().listWorkflowExecutions(listRequest);
//            List<WorkflowExecutionInfo> list = listResponse.getExecutionsList();
//            List<String> ids = new ArrayList<>();
//            for (WorkflowExecutionInfo info: list) {
//                ids.add(info.getExecution().getWorkflowId());
//            }
//            return ids;
//        }
//    }
//    @ActivityInterface
//    public interface SignalActivities {
//        @ActivityMethod
//        LastValue signalUntilClosed(String workflowID) ;
//        @ActivityMethod
//        LastValue signalUntilAndAfterMigrated(String workflowID);
//    }
//
//    public static class SignalActivitiesImpl implements SignalActivities {
//        private final WorkflowClient legacyNamespaceClient;
//        private final WorkflowClient targetNamespaceClient;
//        private final Logger logger = LoggerFactory.getLogger(SignalActivitiesImpl.class);
//        public SignalActivitiesImpl(WorkflowClient legacyNamespaceClient, WorkflowClient targetNamespaceClient) {
//            this.legacyNamespaceClient = legacyNamespaceClient;
//            this.targetNamespaceClient = targetNamespaceClient;
//        }
//
//        @Override
//        public LastValue signalUntilClosed(String workflowID)  {
//            WorkflowServiceStubs service = legacyNamespaceClient.getWorkflowServiceStubs();
//
//            WorkflowExecutionStatus status = WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_RUNNING;
//            LastValue last = new LastValue(workflowID, "UNDEFINED");
//            while(status == WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_RUNNING) {
//                WorkflowExecution execution = WorkflowExecution.newBuilder().setWorkflowId(workflowID).build();
//
//                DescribeWorkflowExecutionResponse description = service.
//                        blockingStub().
//                        describeWorkflowExecution(DescribeWorkflowExecutionRequest.
//                                newBuilder().
//                                setExecution(execution).
//                                setNamespace("default").
//                                build());
//                status = description.getWorkflowExecutionInfo().getStatus();
//
//                if(status == WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_RUNNING) {
//                    LongRunningWorkflow wf = this.legacyNamespaceClient.newWorkflowStub(LongRunningWorkflow.class, workflowID);
//
//                    String value = now();
//                    try {
//                        wf.callThat(value);
//                    }catch(ApplicationFailure e) {
//                        System.console().printf(e.toString());
//                        return last;
//                    }
//                    last = new LastValue(workflowID, value );
//                }
//                try {
//                    Thread.sleep(AppConfig.SIGNAL_FREQUENCY_MILLIS);
//                } catch (InterruptedException e) {
//                    System.console().printf(e.toString());
//                    return last;
//                }
//                Activity.getExecutionContext().heartbeat(null);
//            }
//            System.out.println(String.format("workflowID existing '%s' with status '%s'", workflowID, status));
//            return last;
//        }
//
//        @Override
//        public LastValue signalUntilAndAfterMigrated(String workflowID) {
//            boolean hasMigrated = false;
//            boolean signalable = true;
//
//            WorkflowExecutionStatus status = WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_RUNNING;
//            LastValue last = new LastValue(workflowID, "UNDEFINED");
//            while(signalable) {
//                String value = now();
//                Activity.getExecutionContext().heartbeat(null);
//                if(!hasMigrated) {
//                    try {
//                        LongRunningWorkflow wf = this.legacyNamespaceClient.newWorkflowStub(LongRunningWorkflow.class, workflowID);
//                        wf.callThat(value);
//                        last = new LastValue(workflowID, value);
//                        continue;
//                    } catch (StatusRuntimeException e) {
//                        if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
//                            logger.debug("workflow {} not found...forwarding in target", workflowID);
//                            // swallow this error
//                        }
//                    } catch (ApplicationFailure e) {
//                        System.console().printf(e.toString());
//                    }
//                }
//                try {
//                    LongRunningWorkflow wf = this.targetNamespaceClient.newWorkflowStub(LongRunningWorkflow.class, workflowID);
//                    wf.callThat(value);
//                    hasMigrated = true;
//                    last = new LastValue(workflowID, value);
//                } catch (StatusRuntimeException e) {
//                    if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
//                        logger.debug("workflow {} not found...forwarding in target", workflowID);
//                        // swallow this error
//                        signalable = false;
//                    }
//                } catch (ApplicationFailure e) {
//                    signalable = false;
//                }
//            }
//
//            System.out.println(String.format("workflowID existing '%s' with status '%s'", workflowID, status));
//            return last;
//        }
//    }
//    public static class SimulationWorkflowImpl implements SimulationWorkflow {
//        private static final Logger logger = Workflow.getLogger(SimulationWorkflowImpl.class);
//
//
//        private Map<String, String> lastValues;
//        private final ExecutionActivities executionActivities = Workflow.newActivityStub(
//                ExecutionActivities.class,ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(10)).build());
//
//        private final SignalActivities signalActivities = Workflow.newActivityStub(
//                SignalActivities.class, ActivityOptions.newBuilder().
//                        setHeartbeatTimeout(Duration.ofMinutes(1)).
//                        setStartToCloseTimeout(Duration.ofHours(1)).build());
//
//        @Override
//        public void simulate(SimulationWorkflowParams params) {
//            this.lastValues = new HashMap<>();
//            List<String> executions = executionActivities.getWorkflowIDs();
//            List<Promise<String>> promises = new ArrayList<>();
//
//            if(params.isFailover()) {
//                for (String id : executions) {
//                    promises.add(Async.function(signalActivities::signalUntilAndAfterMigrated, id).
//                            thenApply(lastValue -> lastValues.put(lastValue.workflowId, lastValue.value)));
//                }
//            } else {
//                for (String id : executions) {
//                    promises.add(Async.function(signalActivities::signalUntilClosed, id).
//                            thenApply(lastValue -> lastValues.put(lastValue.workflowId, lastValue.value)));
//                }
//            }
//
//            Promise.allOf(promises).get();
//            logger.info("completed signals sending");
//        }
//
//        @Override
//        public String getLastValue(String workflowId) {
//            return this.lastValues.get(workflowId);
//        }
//    }
//}
