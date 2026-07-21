package net.dysky.planner.hotel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CenterDTO(
        BigDecimal lat,
        BigDecimal lon
) {
}