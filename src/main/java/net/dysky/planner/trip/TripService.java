package net.dysky.planner.trip;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.dysky.planner.exception.TripNotFoundException;
import net.dysky.planner.group.CreateGroupDTO;
import net.dysky.planner.group.GroupService;
import net.dysky.planner.tripItem.TripItem;
import net.dysky.planner.tripItem.TripItemService;
import net.dysky.planner.tripitinerary.CreateTripItineraryDTO;
import net.dysky.planner.tripitinerary.TripItineraryService;
import net.dysky.planner.tripitinerary.UpdateTripItineraryDTO;
import net.dysky.planner.user.User;
import net.dysky.planner.user.UserService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;

    private final TripItemService tripItemService;

    private final TripItineraryService tripItineraryService;

    private final UserService userService;

    private final GroupService groupService;

    public Trip getTripById(UUID id) {
        return tripRepository.findById(id).orElseThrow(
                () -> new TripNotFoundException("Trip not found"));
    }

    public List<Trip> getAllTrips() {
        return tripRepository.findAll();
    }

    public List<Trip> getTripsByStatus(TripStatus status) {
        return tripRepository.findByStatus(status);
    }

    public List<Trip> getAllTripsByEmail(String email) {
        return tripRepository.findAllByTripCreatorEmail(email);
    }

    public List<Trip> getAllTripsByEmailAndStatus(String email, TripStatus tripStatus) {
        return tripRepository.findAllByTripCreatorEmailAndStatus(email, tripStatus);
    }

    @Transactional
    public Trip updateTripCosts(UUID tripId) {
        Trip trip = getTripById(tripId);
        Double totalCost = tripItineraryService.getTotalCostByTripId(tripId);
        if(totalCost + trip.getBudget() > trip.getBudget()) {
            throw new RuntimeException("Total cost exceeds budget");
        }

        trip.setActualCost(totalCost);

        return tripRepository.save(trip);
    }

    @Transactional
    public Trip createTrip(CreateTripDTO createTripDTO, String email) {
        Trip trip = new Trip();

        trip.setName(createTripDTO.name());
        trip.setDestination(createTripDTO.destination());
        trip.setStatus(TripStatus.PLANNED);

        User user = userService.getUserByEmail(email);
        trip.setTripCreator(user);

        if(createTripDTO.budget() <= 0) {
            throw new RuntimeException("Budget must be a positive value");
        }

        trip.setBudget(createTripDTO.budget());

        // TODO group
        trip.setTripGroup(groupService.createGroup(new CreateGroupDTO(""), email));

        trip.setStartDate(createTripDTO.startDate());
        trip.setEndDate(createTripDTO.endDate());

        return tripRepository.save(trip);
    }

    @Transactional
    public Trip updateTrip(UUID id, UpdateTripDTO updateTripDTO) {
        Trip trip = getTripById(id);

        if (updateTripDTO.name() != null) {
            trip.setName(updateTripDTO.name());
        }
        if (updateTripDTO.destination() != null) {
            trip.setDestination(updateTripDTO.destination());
        }
        if (updateTripDTO.status() != null) {
            trip.setStatus(updateTripDTO.status());
        }
        if (updateTripDTO.budget() != null) {
            trip.setBudget(updateTripDTO.budget());
        }
        if (updateTripDTO.startDate() != null) {
            trip.setStartDate(updateTripDTO.startDate());
        }
        if (updateTripDTO.endDate() != null) {
            trip.setEndDate(updateTripDTO.endDate());
        }
        if(updateTripDTO.group() != null) {
            trip.setTripGroup(updateTripDTO.group());
        }

        return tripRepository.save(trip);
    }

    @Transactional
    public void deleteTrip(UUID id) {
        Trip trip = getTripById(id);
        tripRepository.delete(trip);
    }

    @Transactional
    public void addTripItemToTrip(UUID tripId, CreateTripItineraryDTO createTripItineraryDTO) {
        Trip trip = getTripById(tripId);

        TripItem tripItem = tripItemService.findByName(createTripItineraryDTO.name());

        tripItineraryService.addTripItinerary(trip, tripItem);

        updateTripCosts(tripId);
    }

    @Transactional
    public void updateTripItemInTrip(UUID tripId,  UpdateTripItineraryDTO updateTripItineraryDTO) {
        TripItem tripItem = tripItemService.findByName(updateTripItineraryDTO.name());
        Trip trip = getTripById(tripId);

        tripItineraryService.updateTripItinerary(trip, tripItem, updateTripItineraryDTO.price());

        updateTripCosts(tripId);
    }

    @Transactional
    public void removeTripItemFromTrip(UUID tripId, String name) {
        Trip trip = getTripById(tripId);
        TripItem tripItem = tripItemService.findByName(name);

        tripItineraryService.deleteTripItinerary(trip.getId(), tripItem.getId());

        updateTripCosts(tripId);
    }

}
