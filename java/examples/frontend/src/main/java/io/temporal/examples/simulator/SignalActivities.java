package io.temporal.examples.simulator;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface SignalActivities {
    @ActivityMethod
    SignalDetailsResponse signalUntilClosed(SignalParams params);

    @ActivityMethod
    SignalDetailsResponse signalUntilAndAfterMigrated(SignalParams params);
}
