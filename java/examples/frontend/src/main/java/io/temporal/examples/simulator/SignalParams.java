package io.temporal.examples.simulator;

public class SignalParams {
    private long signalFrequencyMillis;
    private String workflowId;

    public SignalParams(String workflowId, long signalFrequencyMillis, int signalTargetThresholdCount) {
        this.signalFrequencyMillis = signalFrequencyMillis;
        this.workflowId = workflowId;
        this.signalTargetThresholdCount = signalTargetThresholdCount;
    }

    private int signalTargetThresholdCount;

    public SignalParams() {
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

    public int getSignalTargetThresholdCount() {
        return signalTargetThresholdCount;
    }

    public void setSignalTargetThresholdCount(int signalTargetThresholdCount) {
        this.signalTargetThresholdCount = signalTargetThresholdCount;
    }
}
