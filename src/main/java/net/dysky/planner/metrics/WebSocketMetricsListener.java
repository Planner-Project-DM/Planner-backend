package net.dysky.planner.metrics;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;

@Component
@RequiredArgsConstructor
public class WebSocketMetricsListener {

    private final AppMetrics appMetrics;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        appMetrics.incrementActiveWebSocketConnections();
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionConnectedEvent event) {
        appMetrics.decrementActiveWebSocketConnections();
    }
}
