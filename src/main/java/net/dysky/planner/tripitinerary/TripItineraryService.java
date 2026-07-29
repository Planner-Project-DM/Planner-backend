package net.dysky.planner.tripitinerary;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.dysky.planner.trip.Trip;
import net.dysky.planner.tripItem.TripItem;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class TripItineraryService {

    private final TripItineraryRepository tripItineraryRepository;

    public TripItinerary findById(UUID tripId, UUID tripItemId) {
        TripTripItemsId tripTripItemsId = new TripTripItemsId(tripId, tripItemId);

        return tripItineraryRepository.findById(tripTripItemsId).orElseThrow(
                () -> new RuntimeException("Trip Itinerary not found"));
    }

    @Transactional
    public TripItinerary addTripItinerary(Trip trip, TripItem tripItem) {
        TripItinerary tripItinerary = new TripItinerary();

        tripItinerary.setTrip(trip);
        tripItinerary.setTripItem(tripItem);
        tripItinerary.setPrice(0.0);
        return tripItineraryRepository.save(tripItinerary);
    }

    @Transactional
    public TripItinerary updateTripItinerary(Trip trip, TripItem tripItem, Double price) {
        TripItinerary tripItinerary = findById(trip.getId(), tripItem.getId());
        tripItinerary.setPrice(price);

        return tripItineraryRepository.save(tripItinerary);
    }

    @Transactional
    public void deleteTripItinerary(UUID tripId, UUID tripItemId) {
        TripTripItemsId tripTripItemsId = new TripTripItemsId(tripId, tripItemId);
        tripItineraryRepository.deleteById(tripTripItemsId);
    }

}
