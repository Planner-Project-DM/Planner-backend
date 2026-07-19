package net.dysky.planner.trip;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.dysky.planner.exception.TripNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;

    public Trip getTripById(UUID id) {
        return tripRepository.findById(id).orElseThrow(() -> new TripNotFoundException("Trip not found"));
    }

    public List<Trip> getAllTrips() {
        return tripRepository.findAll();
    }

    public List<Trip> getTripsByStatus(TripStatus status) {
        return tripRepository.findByStatus(status);
    }

    @Transactional
    public Trip createTrip(CreateTripDTO createTripDTO) {
        Trip trip = new Trip();

        trip.setName(createTripDTO.name());
        trip.setDestination(createTripDTO.destination());
        trip.setStatus(TripStatus.PLANNED);

        // TODO
        trip.setTripCreator(null);

        if(createTripDTO.budget() <= 0) {
            throw new IllegalArgumentException("Budget must be a positive value");
        }

        trip.setBudget(createTripDTO.budget());

        // TODO
        trip.setGroup(null);

        trip.setStartDate(createTripDTO.startDate());
        trip.setEndDate(createTripDTO.endDate());

        return tripRepository.save(trip);

    }

}
