package io.temporal.examples.simulator;

public class SimulationWorkflowParams {
    private boolean failover;

    public boolean isFailover() {
        return failover;
    }

    public void setFailover(boolean failover) {
        this.failover = failover;
    }

}
