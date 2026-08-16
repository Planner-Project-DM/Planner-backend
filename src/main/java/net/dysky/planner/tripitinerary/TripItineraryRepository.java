package net.dysky.planner.tripitinerary;

import net.dysky.planner.trip.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface TripItineraryRepository extends JpaRepository<TripItinerary, TripTripItemsId> {

    List<TripItinerary> findAllByTripId(UUID tripId);

    boolean existsByTripItem_Id(UUID tripItemId);

    boolean existsByTripAndTripItem_Id(Trip trip, UUID tripItemId);

}
