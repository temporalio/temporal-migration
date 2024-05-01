package io.temporal.examples.simulator;

public class SimulationWorkflowParams {
    private boolean failover;
    private long signalFrequencyMillis;

    private String workflowType;

    private int signalTargetThresholdCount;


    public boolean isFailover() {
        return failover;
    }

    public void setFailover(boolean failover) {
        this.failover = failover;
    }

    public long getSignalFrequencyMillis() {
        return signalFrequencyMillis;
    }

    public void setSignalFrequencyMillis(long signalFrequencyMillis) {
        this.signalFrequencyMillis = signalFrequencyMillis;
    }

    public String getWorkflowType() {
        return workflowType;
    }

    public void setWorkflowType(String workflowType) {
        this.workflowType = workflowType;
    }

    public int getSignalTargetThresholdCount() {
        return signalTargetThresholdCount;
    }

    public void setSignalTargetThresholdCount(int signalTargetThresholdCount) {
        this.signalTargetThresholdCount = signalTargetThresholdCount;
    }
}
