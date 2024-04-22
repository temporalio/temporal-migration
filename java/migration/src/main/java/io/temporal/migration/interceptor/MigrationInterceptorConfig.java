package io.temporal.migration.interceptor;

import io.temporal.common.interceptors.WorkerInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MigrationInterceptorConfig {

    @Bean
    public WorkerInterceptor migrationWorkerInterceptor(Migrator migrator, @Value("{spring.migration.query.name}") String migrationQueryName) {
        return new MigrationWorkerInterceptor(migrator, migrationQueryName);
    }
}
