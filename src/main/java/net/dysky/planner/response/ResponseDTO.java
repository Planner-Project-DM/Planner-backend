package net.dysky.planner.response;

public record ResponseDTO(
        int status,
        String message,
        Object data
) {
}
