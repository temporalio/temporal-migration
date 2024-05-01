package io.temporal.migration.support;

import io.temporal.api.workflowservice.v1.CountWorkflowExecutionsRequest;
import io.temporal.api.workflowservice.v1.CountWorkflowExecutionsResponse;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.SimpleSslContextBuilder;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

@Component
@Configuration
@ConfigurationProperties(prefix = "spring.migration")
public class MigrationProperties {
    private Target target;
    private String stateQueryName;

    private String migrationSignalName;

    public Target getTarget() {
        return target;
    }

    public void setTarget(Target target) {
        this.target = target;
    }

    public String getStateQueryName() {
        return stateQueryName;
    }

    public void setStateQueryName(String stateQueryName) {
        this.stateQueryName = stateQueryName;
    }

    public String getMigrationSignalName() {
        return migrationSignalName;
    }

    public void setMigrationSignalName(String migrationSignalName) {
        this.migrationSignalName = migrationSignalName;
    }

    public static class Target {
        private String namespace;

        public Connection getConnection() {
            return connection;
        }

        public void setConnection(Connection connection) {
            this.connection = connection;
        }

        private Connection connection;

        public String getNamespace() {
            return namespace;
        }

        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }

    }
    public static class Connection {
        private String target;

        private MTLS mtls;

        public MTLS getMtls() {
            return mtls;
        }

        public void setMtls(MTLS mtls) {
            this.mtls = mtls;
        }

        public String getTarget() {
            return target;
        }

        public void setTarget(String target) {
            this.target = target;
        }
    }
    public static class MTLS {
        private String keyFile;
        private String certChainFile;

        public String getKeyFile() {
            return keyFile;
        }

        public void setKeyFile(String keyFile) {
            this.keyFile = keyFile;
        }

        public String getCertChainFile() {
            return certChainFile;
        }

        public void setCertChainFile(String certChainFile) {
            this.certChainFile = certChainFile;
        }
    }
    @Bean(name = "targetTemporalWorkflowClient")
    public WorkflowClient targetTemporalWorkflowClient() throws FileNotFoundException, SSLException {
        WorkflowServiceStubs targetService = null;
        Target topLevelTarget = getTarget();
        Connection connection = getTarget().getConnection();
        String endpoint = getTarget().getConnection().getTarget();
        if(getTarget() != null &&
                getTarget().getConnection() != null &&
                getTarget().getConnection().getTarget() != null &&
                getTarget().getConnection().getMtls() != null) {



            try {
                InputStream clientCert = new FileInputStream(getTarget().getConnection().getMtls().getCertChainFile());
                InputStream clientKey = new FileInputStream(getTarget().getConnection().getMtls().getKeyFile());

                targetService = WorkflowServiceStubs.newServiceStubs(
                        WorkflowServiceStubsOptions.newBuilder()
                                .setSslContext(SimpleSslContextBuilder.forPKCS8(clientCert, clientKey).build())
                                .setTarget(getTarget().getConnection().getTarget())
                                .build());
                CountWorkflowExecutionsResponse countWorkflowExecutionsResponse = targetService.blockingStub().
                        countWorkflowExecutions(CountWorkflowExecutionsRequest.newBuilder().
                                setNamespace(getTarget().getNamespace()).build());

            } catch (IOException e) {
                System.err.println("Error loading certificates: " + e.getMessage());
                throw e;
            }
            return WorkflowClient.newInstance(targetService,
                    WorkflowClientOptions.newBuilder()
                            .setNamespace(getTarget().getNamespace())
                            .build());

        }
        throw new RuntimeException("failed to configure a target workflow client");
    }
}
