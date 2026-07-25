package net.dysky.planner.trip;

import java.time.LocalDate;

public record UpdateTripDTO(
        String name,
        String destination,
        TripStatus status,
        Double budget,
        LocalDate startDate,
        LocalDate endDate
) {
}
