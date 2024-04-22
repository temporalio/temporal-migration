package io.temporal.migration.support;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface MigrationSupport {
    @ActivityMethod
    PullLegacyExecutionResponse pullLegacyExecutionInfo(PullLegacyExecutionRequest req);

    @ActivityMethod
    PushTargetExecutionResponse pushToTargetExecution(PushTargetExecutionRequest cmd);
}
