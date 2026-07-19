package net.dysky.planner.trip;

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

}
