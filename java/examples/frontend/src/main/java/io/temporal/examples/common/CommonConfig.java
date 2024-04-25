package io.temporal.examples.common;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
public class CommonConfig {
    @Bean(name = "legacyTemporalWorkflowClient")
    public WorkflowClient getLegacyClient() {
        return WorkflowClient.newInstance(WorkflowServiceStubs.newServiceStubs(
                WorkflowServiceStubsOptions.newBuilder().build()));
    }
}
