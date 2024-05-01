package io.temporal.examples.simulator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SignalDetailsResponse {
    private String workflowId;

    private List<String> sentSignals = new ArrayList<>();
    private int successLegacySignalAttempts;
    private int successTargetSignalAttempts;
    private int failedLegacySignalAttempts;
    private int failedTargetSignalAttempts;

    public SignalDetailsResponse(String workflowId, String value) {
        this.workflowId = workflowId;
    }

    public SignalDetailsResponse() {
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public int getFailedLegacySignalAttempts() {
        return failedLegacySignalAttempts;
    }

    public void setFailedLegacySignalAttempts(int failedLegacySignalAttempts) {
        this.failedLegacySignalAttempts = failedLegacySignalAttempts;
    }

    public int getFailedTargetSignalAttempts() {
        return failedTargetSignalAttempts;
    }

    public void setFailedTargetSignalAttempts(int failedTargetSignalAttempts) {
        this.failedTargetSignalAttempts = failedTargetSignalAttempts;
    }


    public int getSuccessLegacySignalAttempts() {
        return successLegacySignalAttempts;
    }

    public void setSuccessLegacySignalAttempts(int successLegacySignalAttempts) {
        this.successLegacySignalAttempts = successLegacySignalAttempts;
    }

    public int getSuccessTargetSignalAttempts() {
        return successTargetSignalAttempts;
    }

    public void setSuccessTargetSignalAttempts(int successTargetSignalAttempts) {
        this.successTargetSignalAttempts = successTargetSignalAttempts;
    }

    private void addSentSignal(String value) {
        if(this.sentSignals.size() > 100) {
            this.sentSignals.remove(0);
        }
        this.sentSignals.add(value);
    }
    public void markSuccessLegacy(String value) {
        this.addSentSignal(value);
        this.successLegacySignalAttempts = this.successLegacySignalAttempts + 1;
    }
    public void markSuccessTarget(String value) {
        this.addSentSignal(value);
        this.successTargetSignalAttempts = this.successTargetSignalAttempts + 1;
    }
    public void markFailedLegacy(String value) {
        this.addSentSignal(value);
        this.failedLegacySignalAttempts = this.failedLegacySignalAttempts + 1;
    }
    public void markFailedTarget(String value) {
        this.addSentSignal(value);
        this.failedTargetSignalAttempts = this.failedTargetSignalAttempts + 1;
    }
    public List<String> getSentSignals() {
        return this.sentSignals;
    }
}
