package net.dysky.planner.response;

import java.time.LocalDateTime;

public record ResponseDTO(
        LocalDateTime createdAt,
        int status,
        String message,
        String url,
        Object data
) {
}
