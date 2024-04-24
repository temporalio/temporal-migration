package io.temporal.examples.backend;

public class MigrateableWorkflowResult {
    private String value;

    public MigrateableWorkflowResult() {
    }

    public MigrateableWorkflowResult(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
