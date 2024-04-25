package io.temporal.examples.simulator;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface SignalActivities {
    @ActivityMethod
    LastValue signalUntilClosed(SignalParams params);

    @ActivityMethod
    LastValue signalUntilAndAfterMigrated(SignalParams params);
}
