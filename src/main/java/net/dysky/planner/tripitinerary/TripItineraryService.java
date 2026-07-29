package net.dysky.planner.tripitinerary;

import lombok.RequiredArgsConstructor;
import net.dysky.planner.trip.Trip;
import net.dysky.planner.tripItem.TripItem;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TripItineraryService {

    private final TripItineraryRepository tripItineraryRepository;

    public TripItinerary add(Trip trip, TripItem tripItem, Double price) {
        TripItinerary tripItinerary = new TripItinerary();

        tripItinerary.setTrip(trip);
        tripItinerary.setTripItem(tripItem);
        tripItinerary.setPrice(price);
        return tripItineraryRepository.save(tripItinerary);
    }

}
