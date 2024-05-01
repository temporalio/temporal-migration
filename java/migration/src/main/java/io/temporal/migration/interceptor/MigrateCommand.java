package io.temporal.migration.interceptor;

public class MigrateCommand {
    public String workflowId;
    public String workflowType;

    public Object[] arguments;

    public MigrateCommand(){}
    public MigrateCommand(String workflowType, String workflowId, Object ...arguments){
        this.workflowType= workflowType;
        this.workflowId= workflowId;
        this.arguments = arguments;
    }
}
