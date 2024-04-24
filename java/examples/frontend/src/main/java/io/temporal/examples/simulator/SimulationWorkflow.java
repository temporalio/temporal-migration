package io.temporal.examples.simulator;

import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface SimulationWorkflow {
    @WorkflowMethod
    void simulate(SimulationWorkflowParams params);

    @QueryMethod
    String getLastValue(String workflowId);
}
