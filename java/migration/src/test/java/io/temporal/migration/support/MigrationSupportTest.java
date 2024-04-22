package io.temporal.migration.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.testing.TestActivityEnvironment;
import io.temporal.testing.TestEnvironmentOptions;
import io.temporal.testing.TestWorkflowEnvironment;
import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.annotation.DirtiesContext;


import java.time.Duration;
import java.util.UUID;

@SpringBootTest(classes = { MigrationSupportImpl.class})
//@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnableAutoConfiguration()
@DirtiesContext
public class MigrationSupportTest {
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
    public void testPullLegacyExecution() {
        String wfid = UUID.randomUUID().toString();
        PullLegacyExecutionRequest cmd = new PullLegacyExecutionRequest("default",
                wfid,
                "MigrateableWorkflow", 1);

        // start legacy workflow
        MigrateableWorkflow wf = workflowClient.newWorkflowStub(MigrateableWorkflow.class,
                WorkflowOptions.newBuilder().setWorkflowId(cmd.getWorkflowId()).setTaskQueue(MigrateableWorkflowImpl.taskQueue).build());
        MigrateableWorkflowParams params = new MigrateableWorkflowParams(UUID.randomUUID().toString(), 3);
        WorkflowExecution legacyExecution = WorkflowClient.start(wf::execute, params);

        // call back to legacy for migration state
        MigrationSupport actStub = testActivityEnvironment.newActivityStub(MigrationSupport.class);
        PullLegacyExecutionResponse actual = actStub.pullLegacyExecutionInfo(cmd);
        testWorkflowEnvironment.sleep(Duration.ofSeconds(5));
        Assert.assertNotNull(actual.getMigrationState());
        ObjectMapper mapper = new ObjectMapper();
        MigrationState state = mapper.convertValue(actual.getMigrationState(), MigrationState.class);
        Assert.assertEquals("actual value", params.getValue(),  state.getParams().getValue());
        Assert.assertTrue("isMigrated", state.getExecutionState().isMigrated());
        Assert.assertEquals("elapsedTime", 4, actual.getElapsedTime());

    }

    @Test
    public void testResumeInTarget() {
        String wfid = UUID.randomUUID().toString();
        String namespace = testWorkflowEnvironment.getNamespace();
        Object args = new MigrateableWorkflowParams(UUID.randomUUID().toString(),3);
        PushTargetExecutionRequest cmd = new PushTargetExecutionRequest(
                namespace,
                MigratedWorkflowImpl.taskQueue,
                "MigrateableWorkflow",
                wfid,
                args
        );
        MigrationSupport actStub = testActivityEnvironment.newActivityStub(MigrationSupport.class);
        PushTargetExecutionResponse resp = actStub.pushToTargetExecution(cmd);
        Assertions.assertTrue(resp.isStarted());
        MigrateableWorkflow wf = workflowClient.newWorkflowStub(MigrateableWorkflow.class,cmd.getWorkflowId());
        MigrationState targetState = wf.getMigrationState();
        Assertions.assertEquals(args, targetState.getParams());
    }
    @ComponentScan
    public static class Configuration{}
}
