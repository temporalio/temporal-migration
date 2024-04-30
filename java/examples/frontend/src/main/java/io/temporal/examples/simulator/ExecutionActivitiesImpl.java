package io.temporal.examples.simulator;

import com.sun.source.tree.Tree;
import io.temporal.api.common.v1.Payload;
import io.temporal.api.common.v1.Payloads;
import io.temporal.api.workflow.v1.WorkflowExecutionInfo;
import io.temporal.api.workflowservice.v1.ListWorkflowExecutionsRequest;
import io.temporal.api.workflowservice.v1.ListWorkflowExecutionsResponse;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowStub;
import io.temporal.common.WorkflowExecutionHistory;
import io.temporal.common.converter.DefaultDataConverter;
import io.temporal.examples.backend.MigrateableWorkflowParams;
import io.temporal.examples.backend.MigrateableWorkflowResult;
import io.temporal.examples.common.Clients;
import io.temporal.serviceclient.WorkflowServiceStubs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("execution-activities")
public class ExecutionActivitiesImpl implements ExecutionActivities {
    private final WorkflowClient legacyWorkflowClient;
    private final WorkflowClient targetWorkflowClient;
    private final String resultQueryName;

    public ExecutionActivitiesImpl(Clients clients, @Value("${spring.migration.result-query-name}") String resultQueryName) {
        this.legacyWorkflowClient = clients.getLegacyClient();
        this.targetWorkflowClient = clients.getTargetClient();
        this.resultQueryName = resultQueryName;
    }

    @Override
    public List<String> getWorkflowIDs(String workflowType) {
        WorkflowServiceStubs service = legacyWorkflowClient.getWorkflowServiceStubs();
        String q = String.format("WorkflowType='%s' AND ExecutionStatus='Running'", workflowType);
        ListWorkflowExecutionsRequest listRequest = ListWorkflowExecutionsRequest.newBuilder().
                setNamespace(this.legacyWorkflowClient.getOptions().getNamespace()).
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

    @Override
    public VerificationResponse verify(String workflowId, SignalDetailsResponse signalDetails) {
        VerificationResponse response = new VerificationResponse();
        response.setSignalDetails(signalDetails);

        WorkflowStub legacyStub = this.legacyWorkflowClient.newUntypedWorkflowStub(workflowId);
        MigrateableWorkflowResult legacyResult = legacyStub.query(this.resultQueryName, MigrateableWorkflowResult.class);
        response.setTargetExecutionResult(legacyResult);

        //  find out how the workflow was started..informational use only
        WorkflowExecutionHistory history = this.targetWorkflowClient.fetchHistory(workflowId);
        Payloads payloads =
                history.getHistory().getEvents(0).getWorkflowExecutionStartedEventAttributes().getInput();
        for (Payload payload : payloads.getPayloadsList()) {
            // using default data converter..assumes MigrateableWorkflowParams type
            // note if you use custom data converter you would need use it instead of default
            MigrateableWorkflowParams input = DefaultDataConverter.newDefaultInstance()
                    .fromPayload(payload, MigrateableWorkflowParams.class, MigrateableWorkflowParams.class);
            response.setTargetExecutionStartedInput(input);
        }

        // finally get the current value the workflow is reporting
        WorkflowStub targetStub = this.targetWorkflowClient.newUntypedWorkflowStub(workflowId);
        MigrateableWorkflowResult targetResult = targetStub.query(this.resultQueryName, MigrateableWorkflowResult.class);
        response.setTargetExecutionResult(targetResult);

        // dedupe the things and check that the size is the same
        TreeSet<String> signalDetailsSet = new TreeSet<String>();
        TreeSet<String> targetSignalsSet = new TreeSet<>();
        signalDetailsSet.addAll(signalDetails.getSentSignals());
        targetSignalsSet.addAll(targetResult.getReceivedValues());
        response.setTargetSignalsReceivedSize(targetSignalsSet.size());
        response.setSignalsSentSize(signalDetailsSet.size());

        // now compare each element position in the list
//        ArrayList<String> signalDetailsList = new ArrayList<>(signalDetailsSet);
//        ArrayList<String> targetSignalsList = new ArrayList<>(targetSignalsSet);

        return response;
    }
}
