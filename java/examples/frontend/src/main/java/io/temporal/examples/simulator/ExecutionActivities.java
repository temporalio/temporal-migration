package io.temporal.examples.simulator;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

import java.util.List;

@ActivityInterface
public interface ExecutionActivities {
    @ActivityMethod
    List<String> getWorkflowIDs(String workflowType);
    @ActivityMethod
    VerificationResponse verify(String workflowId, SignalDetailsResponse signalDetails);
}
