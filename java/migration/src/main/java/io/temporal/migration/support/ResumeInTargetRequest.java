package io.temporal.migration.support;

public class ResumeInTargetRequest {
    private Object arguments;
    private  String namespace;
    private  String taskQueue;
    private  String workflowType;
    private  String workflowId;

    public ResumeInTargetRequest(String namespace, String taskQueue, String workflowType, String workflowId, Object arguments) {

        this.namespace = namespace;
        this.taskQueue = taskQueue;
        this.workflowType = workflowType;
        this.workflowId = workflowId;
        this.arguments = arguments;
    }

    public ResumeInTargetRequest() {
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

    public Object getArguments() {
        return arguments;
    }

    public void setArguments(Object arguments) {
        this.arguments = arguments;
    }
}
