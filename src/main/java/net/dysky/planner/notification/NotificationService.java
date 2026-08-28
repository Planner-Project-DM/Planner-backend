package net.dysky.planner.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final KafkaTemplate<String, NotificationDTO> kafkaTemplate;

    public void sendNotification(NotificationDTO dto) {
        kafkaTemplate.send("planner-notification", dto.receiverId().toString(), dto);
    }

}
