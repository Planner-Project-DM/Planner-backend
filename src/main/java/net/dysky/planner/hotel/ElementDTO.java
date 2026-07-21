package net.dysky.planner.hotel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ElementDTO(
        String type,
        Long id,
        BigDecimal lat,
        BigDecimal lon,
        CenterDTO center,
        TagsDTO tags
) {
    @Override
    public BigDecimal lat() {
        if (this.lat != null) {
            return this.lat;
        }
        return this.center != null ? this.center.lat() : null;
    }

    @Override
    public BigDecimal lon() {
        if (this.lon != null) {
            return this.lon;
        }
        return this.center != null ? this.center.lon() : null;
    }
}