package net.dysky.planner.hotel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OverpassApiDTO(
        List<ElementDTO> elements
) {
}
