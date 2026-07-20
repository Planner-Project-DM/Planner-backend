package net.dysky.planner.trip;

import java.time.LocalDate;

public record CreateTripDTO(
        String name,
        String destination,
        Double budget,
        String email,
        LocalDate startDate,
        LocalDate endDate
) {
}
