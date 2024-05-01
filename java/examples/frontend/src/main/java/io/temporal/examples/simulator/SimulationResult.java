package io.temporal.examples.simulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimulationResult {
    private Map<String,VerificationResponse> verifications;
    private List<String> workflowIdsWithDeltas;

    public SimulationResult() {
        this.verifications = new HashMap<>();
        this.workflowIdsWithDeltas = new ArrayList<>();
    }
    public SimulationResult(List<String> workflowIds) {
        this.verifications = new HashMap<>();
        this.workflowIdsWithDeltas = new ArrayList<>();
        for (int i = 0; i < workflowIds.size(); i++) {
            this.verifications.put(workflowIds.get(i), null);
        }
    }

    public String setVerificationResponse(String workflowId, VerificationResponse res) {
        this.verifications.put(workflowId, res);
        return workflowId;
    }

    public Map<String, VerificationResponse> getVerifications() {
        return this.verifications;
    }

    public List<String> getWorkflowIdsWithDeltas() {
        return workflowIdsWithDeltas;
    }

    public void setWorkflowIdsWithDeltas(List<String> workflowIdsWithDeltas) {
        this.workflowIdsWithDeltas = workflowIdsWithDeltas;
    }
}
