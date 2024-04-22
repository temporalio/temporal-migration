package io.temporal.migration.interceptor;

import io.temporal.common.interceptors.WorkflowInboundCallsInterceptor;
import io.temporal.common.interceptors.WorkflowInboundCallsInterceptorBase;
import io.temporal.common.interceptors.WorkflowOutboundCallsInterceptor;
import io.temporal.failure.CanceledFailure;
import io.temporal.workflow.CancellationScope;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowInfo;
import io.temporal.workflow.WorkflowLocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MigrationWorkflowInboundCallsInterceptor extends WorkflowInboundCallsInterceptorBase {
    private static final Logger logger = LoggerFactory.getLogger(MigrationWorkflowInboundCallsInterceptor.class);
    private final WorkflowLocal<Boolean> migrated;
    private CancellationScope scope;

    private Migrator migrator;
    private final String migrationQueryName;
    private MigrationWorkflowOutboundCallsInterceptor outInterceptor;

    public MigrationWorkflowInboundCallsInterceptor(WorkflowInboundCallsInterceptor next, Migrator migrator, String migrationQueryName) {
        super(next);
        this.migrator = migrator;
        this.migrationQueryName = migrationQueryName;
        this.migrated = WorkflowLocal.withInitial(() -> false);
    }

    @Override
    public void init(WorkflowOutboundCallsInterceptor outboundCalls) {
        outInterceptor = new MigrationWorkflowOutboundCallsInterceptor(outboundCalls);
        super.init(outInterceptor);
    }

    @Override
    public WorkflowOutput execute(WorkflowInput input) {
        if(migrated.get()) {
            return super.execute(input);
        }
        WorkflowInfo info = Workflow.getInfo();
        WorkflowLocal<WorkflowOutput> value = WorkflowLocal.withInitial(() -> new WorkflowOutput(null));
        try {
            scope = Workflow.newCancellationScope(() ->{
                value.set(super.execute(input));
            });
            this.outInterceptor.setScope(scope);
            scope.run();
            return value.get();
        } catch( CanceledFailure e) {
            Workflow.newDetachedCancellationScope(() -> {
                QueryOutput q = handleQuery(new QueryInput(migrationQueryName, null, null));
                Object migrateableValue = new WorkflowOutput(q.getResult());
                migrator.migrate(new MigrateCommand(info.getWorkflowType(), info.getWorkflowId(), migrateableValue));
                this.migrated.set(true);
            }).run();
            return value.get();
        }
    }
    /*
    chad notes
    1. test cases for longrunning activity...what happens within a workflow that has such a thing?
    2. test long running signal handlers that are not returning
     */

    @Override
    public void handleSignal(SignalInput input) {
        WorkflowInfo info = Workflow.getInfo();
        if(!migrated.get()) {
            super.handleSignal(input);
            return;
        }
        logger.info("forwarding signal {} for wid {} with {}", input.getSignalName(),info.getWorkflowId(), input.getArguments());
        migrator.forwardSignal(new ForwardSignalCommand(info.getWorkflowType(),
                info.getWorkflowId(),
                input.getSignalName(),
                input.getArguments()));
    }
}
