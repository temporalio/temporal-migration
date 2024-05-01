package io.temporal.examples.backend;


import io.temporal.migration.interceptor.MigrationWorkerInterceptor;
import io.temporal.spring.boot.TemporalOptionsCustomizer;
import io.temporal.worker.WorkerFactoryOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Nonnull;
import java.util.Objects;

@Configuration
@ComponentScan
public class TemporalOptionsConfig {
    Logger logger = LoggerFactory.getLogger(TemporalOptionsConfig.class);
    @Autowired
    private MigrationWorkerInterceptor migrationWorkerInterceptor;

    @Value("${spring.application.name}")
    private String applicationName;

    @Bean
    public TemporalOptionsCustomizer<WorkerFactoryOptions.Builder> customWorkerFactoryOptions() {
        return new TemporalOptionsCustomizer<>() {
            @Nonnull
            @Override
            public WorkerFactoryOptions.Builder customize(
                    @Nonnull WorkerFactoryOptions.Builder optionsBuilder) {
                if(Objects.equals("temporal-migration-legacy", applicationName)) {
                    optionsBuilder.setWorkerInterceptors(migrationWorkerInterceptor);
                }
                return optionsBuilder;
            }
        };
    }
}