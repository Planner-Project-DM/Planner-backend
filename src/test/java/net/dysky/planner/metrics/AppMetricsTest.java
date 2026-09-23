package net.dysky.planner.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class AppMetricsTest {

    private MeterRegistry meterRegistry;
    private AppMetrics appMetrics;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        appMetrics = new AppMetrics(meterRegistry);
    }

    @Test
    void incrementTripCreated_shouldIncreaseCounter() {
        appMetrics.incrementTripCreated();

        double count = meterRegistry.get("planner_trip_created_total")
                .tag("module", "trip")
                .counter()
                .count();

        assertThat(count).isEqualTo(1.0);
    }

    @Test
    void getAuthLoginTimer_shouldReturnTimerAndRecordDuration() {
        var timer = appMetrics.getAuthLoginTimer();

        assertThat(timer).isNotNull();

        timer.record(250, TimeUnit.MILLISECONDS);

        assertThat(timer.count()).isEqualTo(1L);
        assertThat(timer.totalTime(TimeUnit.MILLISECONDS)).isEqualTo(250.0);
    }

    @Test
    void incrementActiveWebSocketConnections_shouldIncreaseGaugeValue() {
        appMetrics.incrementActiveWebSocketConnections();

        double value = meterRegistry.get("planner_websocket_active_connections")
                .gauge()
                .value();

        assertThat(value).isEqualTo(1.0);
    }

    @Test
    void decrementActiveWebSocketConnections_shouldDecreaseGaugeValue() {
        appMetrics.incrementActiveWebSocketConnections();
        appMetrics.incrementActiveWebSocketConnections();
        appMetrics.decrementActiveWebSocketConnections();

        double value = meterRegistry.get("planner_websocket_active_connections")
                .gauge()
                .value();

        assertThat(value).isEqualTo(1.0);
    }
}