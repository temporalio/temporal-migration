package io.temporal.examples.backend;

import java.util.ArrayList;
import java.util.List;

public class MigrateableWorkflowParams {
    private List<String> receivedValues = new ArrayList<>();

    private ExecutionState executionState;

    private int keepAliveDurationSecs;

    public MigrateableWorkflowParams() {
        this.executionState = new ExecutionState(false);
    }

    public void appendValue(String... value) {
        for (String s : value) {
            // poor man's fifo fixed size queue
            if (this.receivedValues.size() > 100) {
                this.receivedValues.remove(0);
            }
            this.receivedValues.add(s);
        }
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


    public List<String> getReceivedValues() {
        return receivedValues;
    }

    public void setReceivedValues(List<String> receivedValues) {
        this.receivedValues = receivedValues;
    }
}
