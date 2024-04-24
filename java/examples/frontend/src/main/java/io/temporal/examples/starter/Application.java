package io.temporal.examples.starter;

import io.temporal.client.WorkflowClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.UUID;

@SpringBootApplication
public class Application implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        // calculate a random value we can use for the duration of this process as the suffix for the task queue
        // to which we route replies from our domain.
        // NOTE that ${random.uuid} in properties will not work since you receive a new random value per-component under SpringBoot's creation
        System.setProperty("APP_UUID", UUID.randomUUID().toString());
        SpringApplication.run(io.temporal.examples.starter.Application.class, args);
    }

    @Autowired
    Clients clients;

    @Value("${app.id-seed}")
    String idSeed;

    @Value("${app.execution-count}")
    int executionCount;

    @Value("${app.temporal.legacy.task-queue}")
    String legacyTaskQueue;

    @Override
    public void run(String... args) throws Exception {
        WorkflowClient legacy = clients.getLegacyClient();
        String foo = "bar";
        logger.info("idSeed {}", idSeed);
        logger.info("legacyTaskQueue {}", legacyTaskQueue);
//        try {
//            for (int i = 0; i < executionCount; i++) {
//                String wid = String.format("%s-%d", idSeed, i);
//                WorkflowOptions workflowOptions = WorkflowOptions.newBuilder().
//                        setWorkflowId(wid).
//                        setTaskQueue(legacyTaskQueue).build();
//                LongRunningWorkflow workflow = legacy.newWorkflowStub(LongRunningWorkflow.class, workflowOptions);
//
//                LongRunningWorkflowParams params = new LongRunningWorkflowParams();
//                params.value = String.format("initial-%d", i);
//                WorkflowClient.start(workflow::execute, params);
//            }
//        } catch( Exception e) {
//            logger.error("an error was encountered trying to start workflows...aborting: " + e);
//            System.exit(1);
//        }
//
//        Simulation.SimulationWorkflow simulationWorkflow = legacy.
//                newWorkflowStub(Simulation.SimulationWorkflow.class, WorkflowOptions.newBuilder().
//                        setWorkflowId(String.format("%s-simulator", idSeed)).setTaskQueue(AppConfig.TASK_QUEUE).build());
//        try {
//            Simulation.SimulationWorkflowParams params = new Simulation.SimulationWorkflowParams();
//            params.setFailover(AppConfig.SIMULATOR_FAILOVER);
//            CompletableFuture<Void> execution = WorkflowClient.execute(simulationWorkflow::simulate, params);
//            logger.info("simulation started: " + execution);
//        } catch( Exception e) {
//            logger.error("simulation failed to start: " + e);
//            System.exit(1);
//        }
    }
}
