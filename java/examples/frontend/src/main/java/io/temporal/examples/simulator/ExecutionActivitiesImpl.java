package io.temporal.examples.simulator;

import io.temporal.api.workflow.v1.WorkflowExecutionInfo;
import io.temporal.api.workflowservice.v1.ListWorkflowExecutionsRequest;
import io.temporal.api.workflowservice.v1.ListWorkflowExecutionsResponse;
import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;

import java.util.ArrayList;
import java.util.List;

public class ExecutionActivitiesImpl implements ExecutionActivities {
    private final WorkflowClient legacyNamespaceClient;

    public ExecutionActivitiesImpl(WorkflowClient legacyNamespaceClient) {
        this.legacyNamespaceClient = legacyNamespaceClient;
    }

    @Override
    public List<String> getWorkflowIDs() {
        WorkflowServiceStubs service = legacyNamespaceClient.getWorkflowServiceStubs();
        // 1. load workflowIDs to consider
        // 2. send `callGiraffe` signal every 200ms to each workflow in the list if the workflow exists in legacy namespace
        // 3. exit when all workflows no longer appear in legacy namespace
        ListWorkflowExecutionsRequest listRequest = ListWorkflowExecutionsRequest.newBuilder().
                setNamespace("default").
                setQuery("WorkflowType='LongRunningWorkflow' AND ExecutionStatus='Running'").
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
