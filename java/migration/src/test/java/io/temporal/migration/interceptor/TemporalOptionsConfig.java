package io.temporal.migration.interceptor;


import io.temporal.client.WorkflowClientOptions;
import io.temporal.spring.boot.TemporalOptionsCustomizer;
import io.temporal.worker.WorkerFactoryOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Nonnull;

@Configuration
@ComponentScan
public class TemporalOptionsConfig {
    Logger logger = LoggerFactory.getLogger(TemporalOptionsConfig.class);
    @Autowired
    private MigrationWorkerInterceptor migrationWorkerInterceptor;
//    @Bean
//    public WorkerOptionsCustomizer customWorkerOptions() {
//        return new WorkerOptionsCustomizer() {
//            @Nonnull
//            @Override
//            public WorkerOptions.Builder customize(
//                    @Nonnull WorkerOptions.Builder optionsBuilder,
//                    @Nonnull String workerName,
//                    @Nonnull String taskQueue) {
//
//                return optionsBuilder;
//            }
//        };
//    }
//
//    // WorkflowServiceStubsOptions customization
//    // This is more advanced usage, where one can do things like interceptors or other rules on
//    // the underlying gRPC API for the SDK
//    @Bean
//    public TemporalOptionsCustomizer<WorkflowServiceStubsOptions.Builder>
//    customServiceStubsOptions() {
//        return new TemporalOptionsCustomizer<>() {
//            @Nonnull
//            @Override
//            public WorkflowServiceStubsOptions.Builder customize(
//                    @Nonnull WorkflowServiceStubsOptions.Builder optionsBuilder) {
//                // set options on optionsBuilder as needed
//                // ...
//                return optionsBuilder;
//            }
//        };
//    }
//
//
//    // WorkflowClientOption customization
//    // One might tune the client being used with things like using a Data Converter, a dynamic
//    // Namespace name (instead of from the resources config)
//    @Bean
//    public TemporalOptionsCustomizer<WorkflowClientOptions.Builder> customClientOptions() {
//        return new TemporalOptionsCustomizer<>() {
//            @Nonnull
//            @Override
//            public WorkflowClientOptions.Builder customize(
//                    @Nonnull WorkflowClientOptions.Builder optionsBuilder) {
//
//
//                // with CODEC
////                optionsBuilder.setDataConverter(new CodecDataConverter(DefaultDataConverter.newDefaultInstance(), Collections.singletonList(payloadCodec)));
//                // set options on optionsBuilder as needed
//                // ...
//                return optionsBuilder;
//            }
//        };
//    }

    @Bean
    public TemporalOptionsCustomizer<WorkerFactoryOptions.Builder> customWorkerFactoryOptions() {
        return new TemporalOptionsCustomizer<>() {
            @Nonnull
            @Override
            public WorkerFactoryOptions.Builder customize(
                    @Nonnull WorkerFactoryOptions.Builder optionsBuilder) {
                optionsBuilder.setWorkerInterceptors(migrationWorkerInterceptor);
                return optionsBuilder;
            }
        };
    }
}