package io.temporal.examples.simulator;

import java.util.Objects;

public class LastValue {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LastValue lastValue = (LastValue) o;
        return Objects.equals(workflowId, lastValue.workflowId) && Objects.equals(value, lastValue.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workflowId, value);
    }
}
