package net.dysky.planner.notification;

import java.util.UUID;

public record NotificationDTO(
        String title,
        String message,
        UUID receiverId
) {
}
