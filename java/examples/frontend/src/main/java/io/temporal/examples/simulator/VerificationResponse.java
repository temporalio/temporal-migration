package io.temporal.examples.simulator;

import io.temporal.examples.backend.MigrateableWorkflowParams;
import io.temporal.examples.backend.MigrateableWorkflowResult;

import java.util.ArrayList;
import java.util.List;

public class VerificationResponse {
    private SignalDetailsResponse signalDetails ;

    private MigrateableWorkflowParams targetExecutionStartedInput;
    private MigrateableWorkflowResult targetExecutionResult;
    private MigrateableWorkflowResult legacyExecutionResult;

    private int targetSignalsReceivedSize = 0;
    private int signalsSentSize = 0;

    public List<String> getDroppedSignals() {
        return droppedSignals;
    }

    public void setDroppedSignals(List<String> droppedSignals) {
        this.droppedSignals = droppedSignals;
    }

    private List<String> droppedSignals = new ArrayList<>();

    public SignalDetailsResponse getSignalDetails() {
        return signalDetails;
    }

    public void setSignalDetails(SignalDetailsResponse signalDetails) {
        this.signalDetails = signalDetails;
    }

    public MigrateableWorkflowResult getTargetExecutionResult() {
        return targetExecutionResult;
    }

    public void setTargetExecutionResult(MigrateableWorkflowResult targetExecutionResult) {
        this.targetExecutionResult = targetExecutionResult;
    }

    public MigrateableWorkflowResult getLegacyExecutionResult() {
        return legacyExecutionResult;
    }

    public void setLegacyExecutionResult(MigrateableWorkflowResult legacyExecutionResult) {
        this.legacyExecutionResult = legacyExecutionResult;
    }


    public MigrateableWorkflowParams getTargetExecutionStartedInput() {
        return targetExecutionStartedInput;
    }

    public void setTargetExecutionStartedInput(MigrateableWorkflowParams targetExecutionStartedInput) {
        this.targetExecutionStartedInput = targetExecutionStartedInput;
    }

    public int getTargetSignalsReceivedSize() {
        return targetSignalsReceivedSize;
    }

    public void setTargetSignalsReceivedSize(int targetSignalsReceivedSize) {
        this.targetSignalsReceivedSize = targetSignalsReceivedSize;
    }

    public int getSignalsSentSize() {
        return signalsSentSize;
    }

    public void setSignalsSentSize(int signalsSentSize) {
        this.signalsSentSize = signalsSentSize;
    }
}
