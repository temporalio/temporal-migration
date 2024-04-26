package io.temporal.examples.simulator;

import io.temporal.activity.ActivityOptions;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Async;
import io.temporal.workflow.Promise;
import io.temporal.workflow.Workflow;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WorkflowImpl(taskQueues = SimulationWorkflowImpl.taskQueue)
public class SimulationWorkflowImpl implements SimulationWorkflow {

    public static final String taskQueue = "simulation";
    private static final Logger logger = Workflow.getLogger(SimulationWorkflowImpl.class);

    private Map<String, String> lastValues;
    private final ExecutionActivities executionActivities = Workflow.newActivityStub(
            ExecutionActivities.class, ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(10)).build());

    private final SignalActivities signalActivities = Workflow.newActivityStub(
            SignalActivities.class, ActivityOptions.newBuilder().
                    setHeartbeatTimeout(Duration.ofMinutes(1)).
                    setStartToCloseTimeout(Duration.ofHours(1)).build());

    @Override
    public void simulate(SimulationWorkflowParams params) {
        this.lastValues = new HashMap<>();
        List<String> executions = executionActivities.getWorkflowIDs(params.getWorkflowType());
        List<Promise<String>> promises = new ArrayList<>();

        if (params.isFailover()) {
            for (String id : executions) {
                promises.add(Async.function(signalActivities::signalUntilAndAfterMigrated, new SignalParams(id, params.getSignalFrequencyMillis(), params.getSignalTargetThresholdCount())).
                        thenApply(lastValue -> lastValues.put(lastValue.getWorkflowId(), lastValue.getValue())));
            }
        } else {
            for (String id : executions) {
                promises.add(Async.function(signalActivities::signalUntilClosed, new SignalParams(id, params.getSignalFrequencyMillis(), params.getSignalTargetThresholdCount())).
                        thenApply(lastValue -> lastValues.put(lastValue.getWorkflowId(), lastValue.getValue())));
            }
        }

        Promise.allOf(promises).get();
        logger.info("completed signals sending");
    }

    @Override
    public String getLastValue(String workflowId) {
        return this.lastValues.get(workflowId);
    }
}
