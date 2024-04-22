package io.temporal.migration.interceptor;

import io.temporal.workflow.SignalMethod;

/** Interface used to dynamically register signal from the interceptor. */
public interface MigrationWorkflowInterceptorListener {
    public static final String migrationSignalName = "migrateIt";
    @SignalMethod
    void migrateIt();
}
