package io.temporal.examples.simulator;

import io.temporal.api.workflow.v1.WorkflowExecutionInfo;
import io.temporal.api.workflowservice.v1.ListWorkflowExecutionsRequest;
import io.temporal.api.workflowservice.v1.ListWorkflowExecutionsResponse;
import io.temporal.client.WorkflowClient;
import io.temporal.examples.common.Clients;
import io.temporal.serviceclient.WorkflowServiceStubs;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("execution-activities")
public class ExecutionActivitiesImpl implements ExecutionActivities {
    private final WorkflowClient legacyNamespaceClient;

    public ExecutionActivitiesImpl(Clients clients) {
        this.legacyNamespaceClient = clients.getLegacyClient();
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
