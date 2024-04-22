package io.temporal.migration.support;

import io.temporal.api.enums.v1.WorkflowExecutionStatus;

public class PullLegacyExecutionResponse {
    private WorkflowExecutionStatus status;
    private Object migrationState;
    private boolean isResumable;
    private int elapsedTime;

    public PullLegacyExecutionResponse() {
    }

    public PullLegacyExecutionResponse(WorkflowExecutionStatus status, Object migrationState, boolean isResumable) {
        this.status = status;
        this.migrationState = migrationState;
        this.isResumable = isResumable;
    }

    public WorkflowExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(WorkflowExecutionStatus status) {
        this.status = status;
    }

    public Object getMigrationState() {
        return migrationState;
    }

    public void setMigrationState(Object migrationState) {
        this.migrationState = migrationState;
    }

    public boolean isResumable() {
        return isResumable;
    }

    public void setResumable(boolean resumable) {
        isResumable = resumable;
    }

    public int getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(int elapsedTime) {
        this.elapsedTime = elapsedTime;
    }
}
