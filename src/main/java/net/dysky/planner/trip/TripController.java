package net.dysky.planner.trip;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.dysky.planner.auth.JwtService;
import net.dysky.planner.response.ResponseDTO;
import net.dysky.planner.tripitinerary.CreateTripItineraryDTO;
import net.dysky.planner.tripitinerary.UpdateTripItineraryDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trips")
class TripController {

    private final TripService tripService;

    private final JwtService jwtService;

    @GetMapping
    public ResponseEntity<ResponseDTO> getTrips(@RequestParam(required = false) TripStatus status, HttpServletRequest request) {
        String email = jwtService.extractEmail(request);

        if(status != null) {
            List<Trip> trips = tripService.getAllTripsByEmailAndStatus(email, status);
            ResponseDTO responseDTO = new ResponseDTO(LocalDateTime.now(), 200, "Trips retrieved successfully", "/api/trips?status=" + status, trips);
            return ResponseEntity.ok(responseDTO);
        }

        List<Trip> trips = tripService.getAllTripsByEmail(email);
        ResponseDTO responseDTO = new ResponseDTO(LocalDateTime.now(), 200, "Trips retrieved successfully", "/api/trips", trips);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/{id}")
    private ResponseEntity<ResponseDTO> getTripById(@PathVariable UUID id) {
        Trip trip = tripService.getTripById(id);

        ResponseDTO responseDTO = new ResponseDTO(LocalDateTime.now(), 200, "Trip retrieved successfully", "/api/trips/" + id, trip);
        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping
    public ResponseEntity<ResponseDTO> createTrip(@Valid @RequestBody CreateTripDTO createTripDTO, HttpServletRequest request) {
        String email = jwtService.extractEmail(request);
        Trip trip = tripService.createTrip(createTripDTO, email);

        return ResponseEntity.accepted().body(new ResponseDTO(LocalDateTime.now(), 201, "Trip created successfully", "/api/trips", trip));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ResponseDTO> updateTrip(@PathVariable UUID id, @RequestBody UpdateTripDTO updateTripDTO) {
        Trip updatedTrip = tripService.updateTrip(id, updateTripDTO);

        return ResponseEntity.ok(new ResponseDTO(LocalDateTime.now(), 200, "Trip updated successfully", "/api/trips/" + id, updatedTrip));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDTO> deleteTrip(@PathVariable UUID id) {
        tripService.deleteTrip(id);

        return ResponseEntity.ok(new ResponseDTO(LocalDateTime.now(), 200, "Trip deleted successfully", "/api/trips/" + id, null));
    }

    @PostMapping("/{id}/add-item")
    public ResponseEntity<ResponseDTO> addTripItem(@PathVariable UUID id, @RequestBody CreateTripItineraryDTO createTripItineraryDTO) {
        tripService.addTripItemToTrip(id, createTripItineraryDTO);

        return ResponseEntity.ok(new ResponseDTO(
                LocalDateTime.now(),
                200,
                "Trip item added successfully",
                "/api/trips/" + id + "/add-item",
                null
                )
        );
    }

    @PutMapping("/{id}/set-price")
    public ResponseEntity<ResponseDTO> updateTripItem(@PathVariable UUID id, @RequestBody UpdateTripItineraryDTO updateTripItineraryDTO) {
        tripService.updateTripItemInTrip(id, updateTripItineraryDTO);

        return ResponseEntity.ok(new ResponseDTO(
                LocalDateTime.now(),
                200,
                "Trip item updated successfully",
                "/api/trips/" + id + "/update-item",
                null
                )
        );
    }

    @DeleteMapping("/{id}/remove-item")
    public ResponseEntity<ResponseDTO> removeTripItem(@PathVariable UUID id, @RequestParam String name) {
        tripService.removeTripItemFromTrip(id, name);

        return ResponseEntity.ok(new ResponseDTO(
                LocalDateTime.now(),
                200,
                "Trip item removed successfully",
                "/api/trips/" + id + "/remove-item",
                null
                )
        );
    }

}
