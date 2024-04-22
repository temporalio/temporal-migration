//package io.temporal.migration.support;
//
//import io.temporal.client.WorkflowClient;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.ComponentScan;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//@ComponentScan
//public class MigrationSupportConfig {
//    @Value("${spring.migration.state-query-name}")
//    private String migrationStateQueryName;
//    @Bean
//    public MigrationSupport migrationSupport(WorkflowClient legacyWorkflowClient) {
//        return new MigrationSupportImpl(legacyWorkflowClient, migrationStateQueryName);
////        return new MigrationSupportImpl(legacyWorkflowClient, migrationStateQueryName);
//    }
//}
