package net.dysky.planner.weather;

import java.time.LocalDate;

public record WeatherSearchDTO(
        LocalDate startDate,
        LocalDate endDate
) {
}
