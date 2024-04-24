package io.temporal.examples.backend;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class ExecutionState {
    private boolean isMigrated;
    // other relevant fields here to instruct change of behavior

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExecutionState that = (ExecutionState) o;
        return isMigrated == that.isMigrated;
    }

    @Override
    public int hashCode() {
        return Objects.hash(isMigrated);
    }
}
