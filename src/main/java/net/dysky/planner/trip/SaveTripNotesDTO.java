package net.dysky.planner.trip;

import jakarta.validation.constraints.NotBlank;

public record SaveTripNotesDTO(
        @NotBlank(message = "Content must not be blank") String content,
        Long version
) {
}
