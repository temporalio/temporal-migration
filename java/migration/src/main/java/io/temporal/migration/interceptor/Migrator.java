package io.temporal.migration.interceptor;

import io.temporal.migration.support.MigrationSupport;
import io.temporal.migration.support.PushTargetExecutionRequest;
import io.temporal.workflow.WorkflowInfo;

/*
    Migrator
    Implement this interface for the MigrationWorkflow interceptors to perform the migration
 */
public interface Migrator {
    // migrate performs the work of migration (ie, resuming execution elsewhere) during cancellation of the legacy execution
    void migrate(PushTargetExecutionRequest cmd);

    // isMigrateable is a predicate to answer whether the current execution is a migrateable workflow type
    boolean isMigrateable(WorkflowInfo info);
}
