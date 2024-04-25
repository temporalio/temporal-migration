package io.temporal.examples.seed;

import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.examples.backend.MigrateableWorkflow;
import io.temporal.examples.backend.MigrateableWorkflowParams;
import io.temporal.examples.common.Clients;
import io.temporal.examples.common.CommonConfig;
import io.temporal.examples.simulator.SimulationWorkflow;
import io.temporal.examples.simulator.SimulationWorkflowParams;
import io.temporal.spring.boot.autoconfigure.ServiceStubsAutoConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import java.util.UUID;

@SpringBootApplication(exclude = ServiceStubsAutoConfiguration.class)
@Import(CommonConfig.class)
public class Application implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        // calculate a random value we can use for the duration of this process as the suffix for the task queue
        // to which we route replies from our domain.
        // NOTE that ${random.uuid} in properties will not work since you receive a new random value per-component under SpringBoot's creation
        System.setProperty("APP_UUID", UUID.randomUUID().toString());
        SpringApplication.run(io.temporal.examples.seed.Application.class, args);
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

        Class<MigrateableWorkflow> workflowTypeToMigrate = MigrateableWorkflow.class;

        WorkflowClient legacy = clients.getLegacyClient();
        logger.info("idSeed = {}", idSeed);
        logger.info("legacyTaskQueue = {}", legacyTaskQueue);
        logger.info("signalFrequencyMillis = {}", signalFrequencyMillis);
        logger.info("workflowType = {}", String.format("%s", workflowTypeToMigrate.getSimpleName()));
        try {
            for (int i = 0; i < executionCount; i++) {
                String wid = String.format("%s-%d", idSeed, i);
                WorkflowOptions workflowOptions = WorkflowOptions.newBuilder().
                        setWorkflowId(wid).
                        setTaskQueue(legacyTaskQueue).build();
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
                        setWorkflowId(String.format("%s-simulator", idSeed)).setTaskQueue(simulationTaskQueue).build());
        try {
            SimulationWorkflowParams params = new SimulationWorkflowParams();
            params.setFailover(true);
            params.setWorkflowType(workflowTypeToMigrate.getSimpleName());
            params.setSignalFrequencyMillis(signalFrequencyMillis);
//            CompletableFuture<Void> execution = WorkflowClient.execute(simulationWorkflow::simulate, params);
            WorkflowExecution start = WorkflowClient.start(simulationWorkflow::simulate, params);
            logger.info("simulation started: {}" , start.getWorkflowId());
        } catch( Exception e) {
            logger.error("simulation failed to start" ,  e);
            System.exit(1);
        }
    }
}
