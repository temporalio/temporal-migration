package io.temporal.examples.seed;

import io.github.thibaultmeyer.cuid.CUID;
import io.temporal.api.batch.v1.BatchOperationDeletion;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.api.workflowservice.v1.StartBatchOperationRequest;
import io.temporal.api.workflowservice.v1.StartBatchOperationResponse;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.examples.backend.MigrateableWorkflow;
import io.temporal.examples.backend.MigrateableWorkflowParams;
import io.temporal.examples.common.Clients;
import io.temporal.examples.common.CommonConfig;
import io.temporal.examples.simulator.SimulationProperties;
import io.temporal.examples.simulator.SimulationWorkflow;
import io.temporal.examples.simulator.SimulationWorkflowParams;

import io.temporal.serviceclient.WorkflowServiceStubs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import java.util.UUID;

@SpringBootApplication()
@Import({CommonConfig.class, SimulationProperties.class})
public class   Application implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        // calculate a random value we can use for the duration of this process as the suffix for the task queue
        // to which we route replies from our domain.
        // NOTE that ${random.uuid} in properties will not work since you receive a new random value per-component under SpringBoot's creation
        System.setProperty("APP_UUID", UUID.randomUUID().toString());
        SpringApplication.run(io.temporal.examples.seed.Application.class, args);
    }

    @Autowired
    private Clients clients;

    @Autowired
    SimulationProperties simulationProperties;

    public StartBatchOperationResponse cleanExecutions(WorkflowClient client, String workflowType) {
        String q = String.format("WorkflowType='%s'", workflowType);

        WorkflowServiceStubs svc = client.getWorkflowServiceStubs();
        return svc.blockingStub().startBatchOperation(StartBatchOperationRequest.newBuilder()
                .setNamespace(client.getOptions().getNamespace()).setJobId(UUID.randomUUID().toString())
                .setReason("clean").setVisibilityQuery(q).setDeletionOperation(
                        BatchOperationDeletion.newBuilder().build()).build());
    }
    @Override
    public void run(String... args) throws Exception {
        Class<MigrateableWorkflow> workflowTypeToMigrate = MigrateableWorkflow.class;

        cleanExecutions(clients.getLegacyClient(), workflowTypeToMigrate.getSimpleName());
        cleanExecutions(clients.getTargetClient(), workflowTypeToMigrate.getSimpleName());
        cleanExecutions(clients.getLegacyClient(), SimulationWorkflow.class.getSimpleName());

        WorkflowClient legacy = clients.getLegacyClient();
        logger.info("idSeed = {}", simulationProperties.getIdSeed());
        logger.info("legacyTaskQueue = {}", simulationProperties.getLegacyTaskQueue());
        logger.info("signalFrequencyMillis = {}", simulationProperties.getSignalFrequencyMillis());
        logger.info("workflowType = {}", String.format("%s", workflowTypeToMigrate.getSimpleName()));
        try {
            for (int i = 0; i < simulationProperties.getExecutionCount(); i++) {
                String wid = String.format("%s-%d-%s", simulationProperties.getIdSeed(), i, CUID.randomCUID2(4));
                WorkflowOptions workflowOptions = WorkflowOptions.newBuilder().
                        setWorkflowId(wid).
                        setTaskQueue(simulationProperties.getLegacyTaskQueue()).build();
                MigrateableWorkflow workflow = legacy.newWorkflowStub(workflowTypeToMigrate, workflowOptions);
                MigrateableWorkflowParams params = new MigrateableWorkflowParams(String.format("initial-%d", i), 300);
                WorkflowClient.start(workflow::execute, params);
            }
        } catch( Exception e) {
            logger.error("an error was encountered trying to start workflows...aborting: " + e);
            System.exit(1);
        }

        SimulationWorkflow simulationWorkflow = legacy.
                newWorkflowStub(SimulationWorkflow.class, WorkflowOptions.newBuilder().
                        setWorkflowId(String.format("%s-simulator", simulationProperties.getIdSeed())).setTaskQueue(simulationProperties.getSimulationTaskQueue()).build());
        try {
            SimulationWorkflowParams params = new SimulationWorkflowParams();
            params.setFailover(true);
            params.setWorkflowType(workflowTypeToMigrate.getSimpleName());
            params.setSignalFrequencyMillis(simulationProperties.getSignalFrequencyMillis());
//            CompletableFuture<Void> execution = WorkflowClient.execute(simulationWorkflow::simulate, params);
            WorkflowExecution start = WorkflowClient.start(simulationWorkflow::simulate, params);
            logger.info("simulation started: {}" , start.getWorkflowId());
        } catch( Exception e) {
            logger.error("simulation failed to start" ,  e);
            System.exit(1);
        }
    }
}
