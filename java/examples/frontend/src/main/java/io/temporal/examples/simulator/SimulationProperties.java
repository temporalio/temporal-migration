package io.temporal.examples.simulator;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
@ComponentScan
@Configuration
@ConfigurationProperties(prefix = "spring.simulation")
public class SimulationProperties {
    private long signalFrequencyMillis;
    private String idSeed;
    private int executionCount;

    private String simulationTaskQueue;
    private String legacyTaskQueue;


    public long getSignalFrequencyMillis() {
        return signalFrequencyMillis;
    }

    public void setSignalFrequencyMillis(long signalFrequencyMillis) {
        this.signalFrequencyMillis = signalFrequencyMillis;
    }

    public String getIdSeed() {
        return idSeed;
    }

    public void setIdSeed(String idSeed) {
        this.idSeed = idSeed;
    }

    public int getExecutionCount() {
        return executionCount;
    }

    public void setExecutionCount(int executionCount) {
        this.executionCount = executionCount;
    }

    public String getSimulationTaskQueue() {
        return simulationTaskQueue;
    }

    public void setSimulationTaskQueue(String simulationTaskQueue) {
        this.simulationTaskQueue = simulationTaskQueue;
    }

    public String getLegacyTaskQueue() {
        return legacyTaskQueue;
    }

    public void setLegacyTaskQueue(String legacyTaskQueue) {
        this.legacyTaskQueue = legacyTaskQueue;
    }
}
