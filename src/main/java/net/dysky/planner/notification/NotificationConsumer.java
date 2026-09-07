package net.dysky.planner.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = "planner-notification", groupId = "notification-group")
    public void consume(NotificationDTO dto) {

        messagingTemplate.convertAndSendToUser(dto.receiverId().toString(), "/queue/notifications", dto);
    }
}
