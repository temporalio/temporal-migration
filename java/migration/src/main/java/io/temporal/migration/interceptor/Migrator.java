package io.temporal.migration.interceptor;

import io.temporal.workflow.WorkflowInfo;

/*
    Migrator
    Implement this interface for the MigrationWorkflow interceptors to perform the migration
 */
public interface Migrator {
    // migrate performs the work of migration (ie, resuming execution elsewhere) during cancellation of the legacy execution
    void migrate(MigrateCommand cmd);
    // forwardSignal performs the work of forwarding relevant signals to the target execution
    // this is to avoid a potential race while a workflow is cancelling and resuming execution in the target namespace
    void forwardSignal(ForwardSignalCommand cmd);
    // isMigrateable is a predicate to answer whether the current execution is a migrateable workflow type
    boolean isMigrateable(WorkflowInfo info);
}
