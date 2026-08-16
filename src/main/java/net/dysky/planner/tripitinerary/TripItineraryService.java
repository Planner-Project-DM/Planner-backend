package net.dysky.planner.tripitinerary;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.dysky.planner.exception.TripFoundException;
import net.dysky.planner.trip.Trip;
import net.dysky.planner.tripItem.TripItem;
import net.dysky.planner.tripItem.TripItemService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class TripItineraryService {

    private final TripItineraryRepository tripItineraryRepository;

    private final TripItemService tripItemService;

    public TripItinerary findById(UUID tripId, UUID tripItemId) {
        TripTripItemsId tripTripItemsId = new TripTripItemsId(tripId, tripItemId);

        return tripItineraryRepository.findById(tripTripItemsId).orElseThrow(
                () -> new RuntimeException("Trip Itinerary not found"));
    }

    public boolean existsByTripItem(UUID tripItemId) {
        return tripItineraryRepository.existsByTripItem_Id(tripItemId);
    }

    public Double getTotalCostByTripId(UUID tripId) {
        List<TripItinerary> tripItineraries = tripItineraryRepository.findAllByTripId(tripId);
        return tripItineraries.stream().mapToDouble(TripItinerary::getPrice).sum();
    }

    @Transactional
    public TripItinerary addTripItinerary(Trip trip, CreateTripItineraryDTO createTripItineraryDTO) {
        TripItem tripItem = tripItemService.findById(createTripItineraryDTO.tripItemId());

        if(existsByTripItem(tripItem.getId())) {
            throw new TripFoundException("Trip Item already exists in the itinerary");
        }

        TripItinerary tripItinerary = new TripItinerary();

        tripItinerary.setTrip(trip);
        tripItinerary.setTripItem(tripItem);
        tripItinerary.setPrice(0.0);
        return tripItineraryRepository.save(tripItinerary);
    }

    @Transactional
    public TripItinerary updateTripItinerary(Trip trip, UpdateTripItineraryDTO updateTripItineraryDTO) {
        TripItinerary tripItinerary = findById(trip.getId(), updateTripItineraryDTO.tripItemId());
        tripItinerary.setPrice(updateTripItineraryDTO.price());

        return tripItineraryRepository.save(tripItinerary);
    }

    @Transactional
    public void deleteTripItinerary(UUID tripId, UUID tripItemId) {
        TripTripItemsId tripTripItemsId = new TripTripItemsId(tripId, tripItemId);
        tripItineraryRepository.deleteById(tripTripItemsId);
    }

}
