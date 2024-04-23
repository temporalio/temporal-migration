package io.temporal.migration.interceptor;


import io.temporal.activity.LocalActivityOptions;
import io.temporal.migration.support.MigrationSupport;
import io.temporal.migration.support.MigrationSupportImpl;
import io.temporal.migration.support.PushTargetExecutionRequest;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class MigratorImpl { // implements Migrator{

    Logger logger = LoggerFactory.getLogger(MigratorImpl.class);
    public static List<PushTargetExecutionRequest> pushRequests = new ArrayList<>();
//    @Override
    public void migrate(PushTargetExecutionRequest cmd) {
        // here we can inspect arguments to validate the push to the target
        // or we can ignore the migration altogether
        // what is important is that any kind of call is done inside a LocalActivity
        // not a regular activity
        pushRequests.add(cmd);
//          MigrationSupport stub = Workflow.newLocalActivityStub(
//                MigrationSupport.class,
//                LocalActivityOptions.newBuilder().
//                        setStartToCloseTimeout(Duration.ofSeconds(5)).
//                        build());
//        stub.pushToTargetExecution(cmd);
    }

//    @Override
    public boolean isMigrateable(WorkflowInfo info) {
        return Objects.equals(info.getWorkflowType() ,"MigrateableWorkflow");
    }
}
