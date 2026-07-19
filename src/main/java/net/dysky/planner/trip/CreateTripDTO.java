package net.dysky.planner.trip;

import java.time.LocalDateTime;

public record CreateTripDTO(
        String name,
        String destination,
        Double budget,
        String email,
        LocalDateTime startDate,
        LocalDateTime endDate
) {
}
