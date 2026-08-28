package net.dysky.planner.notification;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {

    @KafkaListener(topics = "planner-notification", groupId = "notification-group")
    public void consume(NotificationDTO dto) {
        System.out.println(dto.message());
    }
}
