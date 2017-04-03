package sde.application.utils;

import org.joda.time.Duration;
import org.joda.time.Instant;
import org.joda.time.Interval;

public class Timer {
    private Instant startTime;

    public Timer() {
        this.startTime = new Instant();
    }

    public Duration getDuration() {
        Interval interval = new Interval(startTime, new Instant());
        return interval.toDuration();
    }

    public long getTimeSince() {
        Interval interval = new Interval(startTime, new Instant());
        return interval.toDuration().getMillis();
    }
}
