package io.temporal.migration.interceptor;

import io.temporal.common.interceptors.WorkflowOutboundCallsInterceptor;
import io.temporal.common.interceptors.WorkflowOutboundCallsInterceptorBase;
import io.temporal.workflow.CancellationScope;
import io.temporal.workflow.Workflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MigrationWorkflowOutboundCallsInterceptor extends WorkflowOutboundCallsInterceptorBase {
    private static final Logger logger = LoggerFactory.getLogger(MigrationWorkflowOutboundCallsInterceptor.class);
    private CancellationScope scope;

    public MigrationWorkflowOutboundCallsInterceptor(WorkflowOutboundCallsInterceptor next) {
        super(next);
        Workflow.registerListener(
                (MigrationWorkflowInterceptorListener) () -> {
                    if(this.scope != null ){
                        scope.cancel();
                    }
                });
    }

    public void setScope(CancellationScope scope) {
        this.scope = scope;
    }
}
