package io.temporal.migration.support;

import java.util.Arrays;
import java.util.Objects;

public class PushTargetExecutionRequest {
    private Object[] arguments;
    private  String namespace;
    private  String taskQueue;
    private  String workflowType;
    private  String workflowId;

    public PushTargetExecutionRequest(String namespace, String taskQueue, String workflowType, String workflowId, Object ...arguments) {

        this.namespace = namespace;
        this.taskQueue = taskQueue;
        this.workflowType = workflowType;
        this.workflowId = workflowId;
        this.arguments = arguments;
    }

    public PushTargetExecutionRequest() {
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getTaskQueue() {
        return taskQueue;
    }

    public void setTaskQueue(String taskQueue) {
        this.taskQueue = taskQueue;
    }

    public String getWorkflowType() {
        return workflowType;
    }

    public void setWorkflowType(String workflowType) {
        this.workflowType = workflowType;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public Object[] getArguments() {
        return arguments;
    }

    public void setArguments(Object ...arguments) {
        this.arguments = arguments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PushTargetExecutionRequest that = (PushTargetExecutionRequest) o;
        return Arrays.equals(arguments, that.arguments) && Objects.equals(namespace, that.namespace) && Objects.equals(taskQueue, that.taskQueue) && Objects.equals(workflowType, that.workflowType) && Objects.equals(workflowId, that.workflowId);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(namespace, taskQueue, workflowType, workflowId);
        result = 31 * result + Arrays.hashCode(arguments);
        return result;
    }
}
