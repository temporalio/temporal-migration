package io.temporal.migration.interceptor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@ComponentScan
@Configuration
public class MigrationInterceptorConfig {
//
//    @Bean
//    public MigrationWorkerInterceptorImpl migrationWorkerInterceptor(Migrator migrator, @Value("{spring.migration.state-query-name}") String migrationQueryName) {
//        return new MigrationWorkerInterceptorImpl(migrator, migrationQueryName);
//    }
}
