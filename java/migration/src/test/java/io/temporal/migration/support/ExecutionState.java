package io.temporal.migration.support;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ExecutionState {
    private boolean isMigrated;

    public ExecutionState() {
    }

    public ExecutionState(boolean isMigrated) {

        this.isMigrated = isMigrated;
    }
    @JsonProperty(value="isMigrated")
    public boolean isMigrated() {
        return isMigrated;
    }


    public void setMigrated(boolean migrated) {
        isMigrated = migrated;
    }
}
