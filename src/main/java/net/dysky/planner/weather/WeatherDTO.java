package net.dysky.planner.weather;

public record WeatherDTO(
        String latitude,
        String longitude,
        String generationtime_ms,
        String utc_offset_seconds,
        String timezone,
        String timezone_abbreviation,
        String elevation,
        DailyUnits daily_units,
        Hourly hourly
) {
}
