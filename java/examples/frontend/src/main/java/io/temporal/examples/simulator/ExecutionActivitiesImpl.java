package io.temporal.examples.simulator;

import io.temporal.api.workflow.v1.WorkflowExecutionInfo;
import io.temporal.api.workflowservice.v1.ListWorkflowExecutionsRequest;
import io.temporal.api.workflowservice.v1.ListWorkflowExecutionsResponse;
import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.spring.boot.ActivityImpl;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@ActivityImpl(taskQueues = SimulationWorkflowImpl.taskQueue)
@Component
public class ExecutionActivitiesImpl implements ExecutionActivities {
    private final WorkflowClient legacyNamespaceClient;

    public ExecutionActivitiesImpl(WorkflowClient legacyNamespaceClient) {
        this.legacyNamespaceClient = legacyNamespaceClient;
    }

    @Override
    public List<String> getWorkflowIDs(String workflowType) {
        WorkflowServiceStubs service = legacyNamespaceClient.getWorkflowServiceStubs();
        String q = String.format("WorkflowType='%s' AND ExecutionStatus='Running'", workflowType);
        ListWorkflowExecutionsRequest listRequest = ListWorkflowExecutionsRequest.newBuilder().
                setNamespace(this.legacyNamespaceClient.getOptions().getNamespace()).
                setQuery(q).
                build();
        ListWorkflowExecutionsResponse listResponse = service.blockingStub().listWorkflowExecutions(listRequest);
        List<WorkflowExecutionInfo> list = listResponse.getExecutionsList();
        List<String> ids = new ArrayList<>();
        for (WorkflowExecutionInfo info : list) {
            ids.add(info.getExecution().getWorkflowId());
        }
        return ids;
    }
}
