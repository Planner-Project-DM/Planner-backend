package net.dysky.planner.notification;

import java.util.UUID;

public record NotificationDTO(
    String message,
    UUID senderId,
    UUID receiverId
) {
}
