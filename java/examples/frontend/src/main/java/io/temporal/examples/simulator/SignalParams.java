package io.temporal.examples.simulator;

public class SignalParams {
    private long signalFrequencyMillis;
    private String workflowId;

    public SignalParams() {
    }

    public SignalParams(String workflowId, long signalFrequencyMillis) {
        this.workflowId = workflowId;
        this.signalFrequencyMillis = signalFrequencyMillis;
    }

    public long getSignalFrequencyMillis() {
        return signalFrequencyMillis;
    }

    public void setSignalFrequencyMillis(long signalFrequencyMillis) {
        this.signalFrequencyMillis = signalFrequencyMillis;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }
}
