package net.dysky.planner.trip;

import lombok.RequiredArgsConstructor;
import net.dysky.planner.response.ResponseDTO;
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

    @GetMapping
    public ResponseEntity<ResponseDTO> getTrips(@RequestParam(required = false) TripStatus status) {
        if(status != null) {
            List<Trip> trips = tripService.getTripsByStatus(status);
            ResponseDTO responseDTO = new ResponseDTO(LocalDateTime.now(), 200, "Trips retrieved successfully", "/api/trips?status=" + status, trips);
            return ResponseEntity.ok(responseDTO);
        }

        List<Trip> trips = tripService.getAllTrips();
        ResponseDTO responseDTO = new ResponseDTO(LocalDateTime.now(), 200, "Trips retrieved successfully", "/api/trips", trips);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/{id}")
    private ResponseEntity<ResponseDTO> getTripById(@PathVariable UUID id) {
        Trip trip = tripService.getTripById(id);

        ResponseDTO responseDTO = new ResponseDTO(LocalDateTime.now(), 200, "Trip retrieved successfully", "/api/trips/" + id, trip);
        return ResponseEntity.ok(responseDTO);
    }

}
