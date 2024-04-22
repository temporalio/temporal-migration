package io.temporal.migration.interceptor;

import io.micrometer.core.lang.NonNull;
import io.temporal.common.interceptors.ActivityInboundCallsInterceptor;
import io.temporal.common.interceptors.WorkerInterceptor;
import io.temporal.common.interceptors.WorkflowInboundCallsInterceptor;
import io.temporal.workflow.Workflow;

public class MigrationWorkerInterceptor implements WorkerInterceptor {


    private final Migrator migrator;
    private final String migrationQueryName;

    public MigrationWorkerInterceptor(@NonNull Migrator migrator, String migrationQueryName) {
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
