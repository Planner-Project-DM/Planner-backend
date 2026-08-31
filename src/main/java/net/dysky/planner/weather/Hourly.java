package net.dysky.planner.weather;

import java.util.List;

public record Hourly(
    List<String> time,
    List<Double> temperature_2m,
    List<Double> apparent_temperature,
    List<Double> precipitation,
    List<Double> precipitation_probability,
    List<Double> weather_code,
    List<Double> cloud_cover,
    List<Boolean> is_day,
    List<Double> wind_speed_10m
) {
}
