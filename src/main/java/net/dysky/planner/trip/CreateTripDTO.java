package net.dysky.planner.trip;

import java.time.LocalDate;

public record CreateTripDTO(
        String name,
        String destination,
        Double budget,
        LocalDate startDate,
        LocalDate endDate
) {
}
