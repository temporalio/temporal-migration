package io.temporal.examples.migrate;

import com.google.common.util.concurrent.ListenableFuture;
import io.temporal.api.batch.v1.BatchOperationSignal;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.api.workflowservice.v1.StartBatchOperationRequest;
import io.temporal.api.workflowservice.v1.StartBatchOperationResponse;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.examples.backend.MigrateableWorkflow;
import io.temporal.examples.backend.MigrateableWorkflowParams;
import io.temporal.examples.common.Clients;
import io.temporal.examples.common.CommonConfig;
import io.temporal.examples.simulator.SimulationWorkflow;
import io.temporal.examples.simulator.SimulationWorkflowParams;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.spring.boot.autoconfigure.ServiceStubsAutoConfiguration;
import io.temporal.workflow.Workflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SpringBootApplication(exclude = ServiceStubsAutoConfiguration.class)
@Import(CommonConfig.class)
public class Application implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        // calculate a random value we can use for the duration of this process as the suffix for the task queue
        // to which we route replies from our domain.
        // NOTE that ${random.uuid} in properties will not work since you receive a new random value per-component under SpringBoot's creation
        System.setProperty("APP_UUID", UUID.randomUUID().toString());
        SpringApplication.run(Application.class, args);
    }

    @Autowired
    Clients clients;

    @Value("${app.id-seed}")
    String idSeed;

    @Value("${app.execution-count}")
    int executionCount;

    @Value("${app.signal-frequency-millis}")
    long signalFrequencyMillis;

    @Value("${app.temporal.legacy.task-queue}")
    String legacyTaskQueue;

    @Value("${app.temporal.simulator.task-queue}")
    String simulationTaskQueue;

    @Override
    public void run(String... args) throws Exception {

        Class<MigrateableWorkflow> migrateableWorkflowType = MigrateableWorkflow.class;
        String q = String.format("WorkflowType='%s' AND ExecutionStatus='Running'", migrateableWorkflowType.getSimpleName());
        WorkflowServiceStubs stubs = clients.getLegacyClient().getWorkflowServiceStubs();
        ListenableFuture<StartBatchOperationResponse> migrateIt = stubs.
                futureStub().
                startBatchOperation(StartBatchOperationRequest.newBuilder()
                        .setNamespace(clients.getLegacyClient().getOptions().getNamespace())
                        .setReason("migration")
                        .setJobId(System.getProperty("APP_UUID")
                        ).setVisibilityQuery(q)
                        .setSignalOperation(BatchOperationSignal.newBuilder().setSignal("migrateIt").build())
                        .build());

        logger.info("started migrateIt batch signal operation for {}", q);
        StartBatchOperationResponse startBatchOperationResponse = migrateIt.get(60, TimeUnit.SECONDS);
        logger.info("batch operation has completed");
    }
}
