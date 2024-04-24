package io.temporal.examples.simulator;

final class LastValue {
    private String workflowId;
    private String value;

    public LastValue(String workflowId, String value) {
        this.value = value;
        this.workflowId = workflowId;
    }

    public LastValue() {
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public String getValue() {
        return value;
    }
}
