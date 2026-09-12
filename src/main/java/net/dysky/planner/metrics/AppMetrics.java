package net.dysky.planner.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class AppMetrics {

    private final Counter tripCreatedCounter;
    private final Timer authLoginTimer;
    private final AtomicInteger activeWebSocketConnections;

    public AppMetrics(MeterRegistry registry) {

        this.tripCreatedCounter = Counter.builder("planner_trip_created_total")
                .description("Total number of trips created")
                .tag("module", "trip")
                .register(registry);

        this.authLoginTimer = Timer.builder("planner_auth_login_duration_seconds")
                .description("Duration of authentication login requests")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);

        this.activeWebSocketConnections = new AtomicInteger(0);
        registry.gauge("planner_websocket_active_connections", activeWebSocketConnections, AtomicInteger::get);
    }

    public void incrementTripCreated() {
        tripCreatedCounter.increment();
    }

    public Timer getAuthLoginTimer() {
         return this.authLoginTimer;
    }

    public void incrementActiveWebSocketConnections() {
        activeWebSocketConnections.incrementAndGet();
    }

    public void decrementActiveWebSocketConnections() {
        activeWebSocketConnections.decrementAndGet();
    }
}
