package io.temporal.examples.backend;

import java.util.List;

public class MigrateableWorkflowResult {
    private MigrateableWorkflowParams params;

    public List<String> getReceivedValues() {
        return this.params.getReceivedValues();
    }

    public MigrateableWorkflowResult() {
        this.params = new MigrateableWorkflowParams();
    }

    public void appendValue(String... value) {
        this.params.appendValue(value);
    }

    public MigrateableWorkflowParams getParams() {
        return params;
    }

    public void setParams(MigrateableWorkflowParams params) {
        this.params = params;
    }

}
