package io.temporal.examples.starter;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.SimpleSslContextBuilder;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

@Component
public class ClientsImpl implements Clients {
    @Override
    public WorkflowClient getLegacyClient() {
        return legacyClient;
    }

    @Override
    public WorkflowClient getTargetClient() {
        return targetClient;
    }

    private WorkflowClient legacyClient;
    private WorkflowClient targetClient;

    private WorkflowClient composedClient;
    public ClientsImpl(
           @Value("${app.temporal.legacy.connection.target.namespace}") String legacyNamespace,
           @Value("${app.temporal.target.connection.target}") String targetNamespace,
           @Value("${app.temporal.target.connection.target}") String targetAddress,
           @Value("${app.temporal.target.connection.mtls.cert-chain-file}") String targetCertChainFile,
            @Value("${app.temporal.target.connection.mtls.key-file}") String targetKeyFile
    ) throws FileNotFoundException, SSLException {
        WorkflowServiceStubs legacyService;
        legacyService = WorkflowServiceStubs.newServiceStubs(
                WorkflowServiceStubsOptions.newBuilder().build());

        WorkflowServiceStubs targetService = null;

        try {
            InputStream clientCert = new FileInputStream(targetCertChainFile);
            InputStream clientKey = new FileInputStream(targetKeyFile);

            targetService = WorkflowServiceStubs.newServiceStubs(
                    WorkflowServiceStubsOptions.newBuilder()
                            .setSslContext(SimpleSslContextBuilder.forPKCS8(clientCert, clientKey).build())
                            .setTarget(targetAddress)
                            .build());

        } catch (IOException e) {
            System.err.println("Error loading certificates: " + e.getMessage());
            throw e;
        }
        /*
         * Get a Workflow service client which can be used to start, Signal, and Query
         * Workflow Executions.
         */
        legacyClient = WorkflowClient.newInstance(legacyService,
                WorkflowClientOptions.newBuilder()
                        .build());

        targetClient = WorkflowClient.newInstance(targetService,
                WorkflowClientOptions.newBuilder()
                        .setNamespace(targetNamespace)
                        .build());
    }

}
