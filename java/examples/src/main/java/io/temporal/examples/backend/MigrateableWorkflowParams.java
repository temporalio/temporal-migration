package io.temporal.examples.backend;

import java.util.Objects;

public class MigrateableWorkflowParams {
    private String value;

    private ExecutionState executionState;

    public MigrateableWorkflowParams(String value, int keepAliveDurationSecs) {
        this.value = value;
        this.keepAliveDurationSecs = keepAliveDurationSecs;
    }

    private int keepAliveDurationSecs;

    public MigrateableWorkflowParams() {
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getKeepAliveDurationSecs() {
        return keepAliveDurationSecs;
    }

    public void setKeepAliveDurationSecs(int keepAliveDurationSecs) {
        this.keepAliveDurationSecs = keepAliveDurationSecs;
    }

    public ExecutionState getExecutionState() {
        return executionState;
    }

    public void setExecutionState(ExecutionState executionState) {
        this.executionState = executionState;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MigrateableWorkflowParams that = (MigrateableWorkflowParams) o;
        return keepAliveDurationSecs == that.keepAliveDurationSecs && Objects.equals(value, that.value) && Objects.equals(executionState, that.executionState);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, executionState, keepAliveDurationSecs);
    }
}
