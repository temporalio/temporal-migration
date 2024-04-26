package io.temporal.examples.migrate;

import com.google.common.util.concurrent.ListenableFuture;
import io.temporal.api.batch.v1.BatchOperationSignal;
import io.temporal.api.workflowservice.v1.StartBatchOperationRequest;
import io.temporal.api.workflowservice.v1.StartBatchOperationResponse;
import io.temporal.examples.backend.MigrateableWorkflow;
import io.temporal.examples.common.Clients;
import io.temporal.examples.common.CommonConfig;
import io.temporal.examples.common.MigrationProperties;
import io.temporal.examples.simulator.SimulationProperties;
import io.temporal.serviceclient.WorkflowServiceStubs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@Import({CommonConfig.class, SimulationProperties.class})
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

    @Autowired
    SimulationProperties simulationProperties;

    @Autowired
    MigrationProperties migrationProperties;

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
                        .setJobId(UUID.randomUUID().toString()
                        ).setVisibilityQuery(q)
                        .setSignalOperation(BatchOperationSignal.newBuilder().setSignal(migrationProperties.getMigrationSignalName()).build())
                        .build());

        logger.info("started migrateIt batch signal operation for {}", q);
        StartBatchOperationResponse startBatchOperationResponse = migrateIt.get(60, TimeUnit.SECONDS);
        logger.info("batch operation has completed");
    }
}
