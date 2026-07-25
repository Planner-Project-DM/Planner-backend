package net.dysky.planner.trip;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.dysky.planner.exception.TripNotFoundException;
import net.dysky.planner.user.User;
import net.dysky.planner.user.UserService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;

    private final UserService userService;

    public Trip getTripById(UUID id) {
        return tripRepository.findById(id).orElseThrow(() -> new TripNotFoundException("Trip not found"));
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

        return tripRepository.save(trip);
    }

}
