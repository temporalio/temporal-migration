package io.temporal.examples.simulator;

import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface SimulationWorkflow {
    @WorkflowMethod
    SimulationResult simulate(SimulationWorkflowParams params);

}
