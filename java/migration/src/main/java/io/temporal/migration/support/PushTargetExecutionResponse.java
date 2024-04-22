package io.temporal.migration.support;

public class PushTargetExecutionResponse {
    private boolean isStarted;

    public PushTargetExecutionResponse() {
    }


    public boolean isStarted() {
        return isStarted;
    }

    public void setStarted(boolean started) {
        isStarted = started;
    }
}
