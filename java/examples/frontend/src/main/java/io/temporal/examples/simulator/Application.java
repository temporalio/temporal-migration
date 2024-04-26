package io.temporal.examples.simulator;

import io.temporal.examples.common.CommonConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import java.util.UUID;

@SpringBootApplication
@Import(CommonConfig.class)
public class Application {

    public static void main(String[] args) {
        // calculate a random value we can use for the duration of this process as the suffix for the task queue
        // to which we route replies from our domain.
        // NOTE that ${random.uuid} in properties will not work since you receive a new random value per-component under SpringBoot's creation
        System.setProperty("APP_UUID", UUID.randomUUID().toString());
        SpringApplication.run(Application.class, args);
    }


}