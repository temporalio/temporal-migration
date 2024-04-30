package io.temporal.examples.simulator;

import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Async;
import io.temporal.workflow.Promise;
import io.temporal.workflow.Workflow;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.*;

public class SimulationWorkflowImpl implements SimulationWorkflow {

    public static final String taskQueue = "simulation";
    private static final Logger logger = Workflow.getLogger(SimulationWorkflowImpl.class);

    private SimulationResult result = new SimulationResult();
    private final ExecutionActivities executionActivities = Workflow.newActivityStub(
            ExecutionActivities.class, ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(60)).build());

    private final SignalActivities signalActivities = Workflow.newActivityStub(
            SignalActivities.class, ActivityOptions.newBuilder().
                    setHeartbeatTimeout(Duration.ofMinutes(1)).
                    setStartToCloseTimeout(Duration.ofHours(1)).build());

    @Override
    public SimulationResult simulate(SimulationWorkflowParams params) {

        List<String> executions = executionActivities.getWorkflowIDs(params.getWorkflowType());
        Map<String, SignalDetailsResponse> signalDetails = new HashMap<>();
        result = new SimulationResult(executions);
        List<Promise<String>> signalPromises = new ArrayList<>();
        List<Promise<String>> verificationPromises = new ArrayList<>();

        if (params.isFailover()) {
            for (String id : executions) {
                signalPromises.add(Async.function(signalActivities::signalUntilAndAfterMigrated, new SignalParams(id, params.getSignalFrequencyMillis(), params.getSignalTargetThresholdCount())).
                        thenApply(lastValue -> { signalDetails.put(lastValue.getWorkflowId(), lastValue); return lastValue.getWorkflowId();}));
            }
        } else {
            for (String id : executions) {
                signalPromises.add(Async.function(signalActivities::signalUntilClosed,
                                new SignalParams(id, params.getSignalFrequencyMillis(), params.getSignalTargetThresholdCount())).
                        thenApply(lastValue -> { signalDetails.put(lastValue.getWorkflowId(), lastValue); return lastValue.getWorkflowId();}));
            }
        }

        Promise.allOf(signalPromises).get();
        // wait for all the things to get done in target
        // poor man's calculus..give 3 seconds for every 4 executions to finish up
        long waitFor = (long)Math.min(3L, Math.abs(Math.floor((double) executions.size() / 4) * 3L ));
        Workflow.sleep( Duration.ofSeconds(waitFor));

        // we stopped all our signaling now lets verify the inputs of each execution in the target
        for(String wid: signalDetails.keySet()) {
            verificationPromises.add(Async.function(executionActivities::verify, wid, signalDetails.get(wid)).thenApply(res ->
                    result.setVerificationResponse(wid, res)));
        }
        Promise.allOf(verificationPromises).get();

        for(Map.Entry<String, VerificationResponse> entry: result.getVerifications().entrySet()) {
            if(!Objects.equals(entry.getValue().getSignalsSentSize(), entry.getValue().getTargetSignalsReceivedSize())) {
                result.getWorkflowIdsWithDeltas().add(entry.getValue().getSignalDetails().getWorkflowId());
            }
        }

        logger.info("completed signals sending");
        return result;
    }

}
