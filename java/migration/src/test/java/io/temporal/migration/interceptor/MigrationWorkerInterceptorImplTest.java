package io.temporal.migration.interceptor;

import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.api.enums.v1.WorkflowExecutionStatus;
import io.temporal.api.workflowservice.v1.DescribeWorkflowExecutionRequest;
import io.temporal.api.workflowservice.v1.DescribeWorkflowExecutionResponse;
import io.temporal.api.workflowservice.v1.WorkflowServiceGrpc;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;
import io.temporal.migration.support.*;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.testing.TestActivityEnvironment;
import io.temporal.testing.TestEnvironmentOptions;
import io.temporal.testing.TestWorkflowEnvironment;
import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@SpringBootTest(classes = {
        MigrationWorkerInterceptorImplTest.Configuration.class,
})
//@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnableAutoConfiguration()
@DirtiesContext
@ComponentScan(basePackageClasses = {
        MigrationSupportConfig.class,
        MigrationInterceptorConfig.class,
        TemporalOptionsConfig.class,
})
public class MigrationWorkerInterceptorImplTest {
    @Autowired
    ConfigurableApplicationContext applicationContext;

    @Autowired
    TestWorkflowEnvironment testWorkflowEnvironment;

    @Autowired
    MigrationSupport sut;


    TestActivityEnvironment testActivityEnvironment;

    @Autowired
    WorkflowClient workflowClient;

    @AfterEach
    public void after() {
        testWorkflowEnvironment.close();
    }
    @BeforeEach
    void beforeEach() {
        testActivityEnvironment = TestActivityEnvironment.newInstance(
                TestEnvironmentOptions.newBuilder()
                        .setUseTimeskipping(true)
                        .build()
        );
        testActivityEnvironment.registerActivitiesImplementations(sut);

        applicationContext.start();
    }

    @Test
    public void testCancellationPushesToTarget() {
        String wfid = UUID.randomUUID().toString();
        PullLegacyExecutionRequest cmd = new PullLegacyExecutionRequest(
                "default",
                wfid,
                "MigrateableWorkflow",
                1);

        // start legacy workflow
        MigrateableWorkflow wf = workflowClient.newWorkflowStub(MigrateableWorkflow.class,
                WorkflowOptions.newBuilder().
                        setWorkflowId(cmd.getWorkflowId()).
                        setTaskQueue(MigrateableWorkflowImpl.taskQueue).
                        build());
        MigrateableWorkflowParams params = new MigrateableWorkflowParams(UUID.randomUUID().toString(), 10);
        //wf.execute(params);
        CompletableFuture<MigrateableWorkflowResult> resultFuture = WorkflowClient.execute(wf::execute, params);
        testWorkflowEnvironment.sleep(Duration.ofSeconds(2));

        WorkflowStub workflowStub = workflowClient.newUntypedWorkflowStub(wfid);
        workflowStub.signal(MigrationWorkflowInterceptorListener.migrationSignalName);

        testWorkflowEnvironment.sleep(Duration.ofSeconds(2));
        WorkflowServiceStubs svc = workflowClient.getWorkflowServiceStubs();
        WorkflowServiceGrpc.WorkflowServiceBlockingStub stub = svc.blockingStub();
        DescribeWorkflowExecutionRequest req = DescribeWorkflowExecutionRequest.newBuilder().
                setNamespace(this.workflowClient.getOptions().getNamespace()).
                setExecution(workflowStub.getExecution()).build();
        DescribeWorkflowExecutionResponse describeWorkflowExecutionResponse = stub.describeWorkflowExecution(req);
        Assertions.assertEquals(WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_COMPLETED,describeWorkflowExecutionResponse.getWorkflowExecutionInfo().getStatus());
        Assertions.assertEquals(1, MigratorImpl.pushRequests.size());
        try {
            MigrateableWorkflowResult migrateableWorkflowResult = resultFuture.get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @ComponentScan
    public static class Configuration{}
}