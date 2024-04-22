package io.temporal.migration.support;

public class PullLegacyExecutionRequest {

    private String namespace;
    private String workflowId;
    private String workflowType;
    private int pollingDurationSecs;


    public PullLegacyExecutionRequest() {
    }

    public PullLegacyExecutionRequest(String namespace, String workflowId, String workflowType, int pollingDurationSecs) {
        this.namespace = namespace;
        this.workflowId = workflowId;
        this.workflowType = workflowType;
        this.pollingDurationSecs = pollingDurationSecs;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public String getWorkflowType() {
        return workflowType;
    }

    public void setWorkflowType(String workflowType) {
        this.workflowType = workflowType;
    }

    public int getPollingDurationSecs() {
        return pollingDurationSecs;
    }

    public void setPollingDurationSecs(int pollingDurationSecs) {
        this.pollingDurationSecs = pollingDurationSecs;
    }
}
