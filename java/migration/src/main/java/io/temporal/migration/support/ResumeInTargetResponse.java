package io.temporal.migration.support;

public class ResumeInTargetResponse {
    private boolean isStarted;

    public ResumeInTargetResponse() {
    }


    public boolean isStarted() {
        return isStarted;
    }

    public void setStarted(boolean started) {
        isStarted = started;
    }
}
