package net.dysky.planner.weather;

import java.util.List;

public record Daily(
        List<String> time,
        List<Double> temperature_2m_max,
        List<Double> temperature_2m_min,
        List<Double> rain_sum
) {
}
