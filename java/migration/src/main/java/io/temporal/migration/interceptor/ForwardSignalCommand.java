package io.temporal.migration.interceptor;

public class ForwardSignalCommand {
    public String workflowId;
    public String workflowType;
    public String signalName;
    public Object[] arguments;

    public ForwardSignalCommand(){}
    public ForwardSignalCommand(String workflowType, String workflowId, String signalName, Object ...arguments) {
        this.workflowType = workflowType;
        this.workflowId = workflowId;
        this.signalName = signalName;
        this.arguments = arguments;
    }
}
