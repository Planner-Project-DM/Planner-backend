package net.dysky.planner.trip;

import net.dysky.planner.tripitem.TripItem;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record TripDetailsDTO(
        UUID id,
        String name,
        String destination,
        TripStatus status,
        Double budget,
        Double actualCost,
        LocalDate startDate,
        LocalDate endDate,
        TripGroupDTO tripGroup,
        List<TripItineraryDTO> tripItineraries
) {
    public record TripGroupDTO(UUID id, String name) {}

    public record TripItineraryDTO(Double price, TripItem tripItem) {}

    public static TripDetailsDTO fromEntity(Trip trip) {
        TripGroupDTO groupDto = null;
        if (trip.getTripGroup() != null) {
            groupDto = new TripGroupDTO(
                    trip.getTripGroup().getId(),
                    trip.getTripGroup().getName()
            );
        }

        List<TripItineraryDTO> itineraryDtos = null;
        if (trip.getTripItineraries() != null) {
            itineraryDtos = trip.getTripItineraries().stream()
                    .map(it -> new TripItineraryDTO(it.getPrice(), it.getTripItem()))
                    .toList();
        }

        return new TripDetailsDTO(
                trip.getId(),
                trip.getName(),
                trip.getDestination(),
                trip.getStatus(),
                trip.getBudget(),
                trip.getActualCost(),
                trip.getStartDate(),
                trip.getEndDate(),
                groupDto,
                itineraryDtos
        );
    }
}