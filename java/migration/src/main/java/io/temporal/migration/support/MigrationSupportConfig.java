package io.temporal.migration.support;

import io.temporal.client.WorkflowClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MigrationSupportConfig {
    @Bean
    public MigrationSupport migrationSupport(WorkflowClient legacyWorkflowClient, @Value("${spring.migration.stateQueryName}") String migrationStateQueryName) {
        return new MigrationSupportImpl(legacyWorkflowClient, migrationStateQueryName);
    }
}
