package net.dysky.planner.weather;

public record DailyUnits(
    String time,
    String temperature_2m_max,
    String temperature_2m_min,
    String rain_sum
) {
}
