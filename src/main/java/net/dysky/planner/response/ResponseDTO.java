package net.dysky.planner.response;

import java.time.LocalDateTime;

public record ResponseDTO(
        LocalDateTime localDateTime,
        int status,
        String message,
        String url,
        Object data
) {
}
