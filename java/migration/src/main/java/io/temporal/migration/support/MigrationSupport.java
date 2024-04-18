package io.temporal.migration.support;

public interface MigrationSupport {
    PullLegacyExecutionResponse pullLegacyExecutionInfo(PullLegacyExecutionRequest req);
}
