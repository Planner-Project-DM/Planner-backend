package net.dysky.planner.tripitinerary;

import java.util.UUID;

public record UpdateTripItineraryDTO(
        UUID tripItemId,
        Double price
) {
}
