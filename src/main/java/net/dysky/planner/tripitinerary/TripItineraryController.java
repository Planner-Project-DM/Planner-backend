package net.dysky.planner.tripitinerary;

import lombok.RequiredArgsConstructor;
import net.dysky.planner.response.ResponseDTO;
import net.dysky.planner.trip.Trip;
import net.dysky.planner.trip.TripService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trips/{id}/item")
class TripItineraryController {

    private final TripService tripService;

    private final TripItineraryService tripItineraryService;

    @PostMapping
    public ResponseEntity<ResponseDTO> addTripItem(@PathVariable UUID id, @RequestBody CreateTripItineraryDTO createTripItineraryDTO) {
        Trip trip = tripService.getTripById(id);

        tripItineraryService.addTripItinerary(trip, createTripItineraryDTO);
        tripService.updateTripCosts(id);

        return ResponseEntity.ok(new ResponseDTO(
                LocalDateTime.now(),
                200,
                "Trip item added successfully",
                "/api/trips/" + id + "/add-item",
                null
            )
        );
    }

}
