package io.temporal.migration.interceptor;

import io.micrometer.core.lang.NonNull;
import io.temporal.common.interceptors.ActivityInboundCallsInterceptor;
import io.temporal.common.interceptors.WorkerInterceptor;
import io.temporal.common.interceptors.WorkflowInboundCallsInterceptor;
import io.temporal.workflow.Workflow;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MigrationWorkerInterceptorImpl implements MigrationWorkerInterceptor {


    private final Migrator migrator;
    private final String migrationQueryName;

    public MigrationWorkerInterceptorImpl(@NonNull Migrator migrator, @Value("${spring.migration.state-query-name}") String migrationQueryName) {
        this.migrator = migrator;
        this.migrationQueryName = migrationQueryName;
    }

    @Override
    public WorkflowInboundCallsInterceptor interceptWorkflow(WorkflowInboundCallsInterceptor next) {

        if(!this.migrator.isMigrateable(Workflow.getInfo())) {
            return next;
        }

        return new MigrationWorkflowInboundCallsInterceptor(next, migrator, migrationQueryName);
    }

    @Override
    public ActivityInboundCallsInterceptor interceptActivity(ActivityInboundCallsInterceptor next) {
        return next;
    }
}
