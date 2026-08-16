package net.dysky.planner.tripSchedule;

import lombok.RequiredArgsConstructor;
import net.dysky.planner.response.ResponseDTO;
import net.dysky.planner.schedule.ScheduleResponseDTO;
import net.dysky.planner.trip.Trip;
import net.dysky.planner.trip.TripService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trips/{id}/schedules")
class TripScheduleController {

    private final TripScheduleService tripScheduleService;

    private final TripService tripService;

    @GetMapping
    public ResponseEntity<ResponseDTO> getAllSchedulesForTrip(@PathVariable("id") UUID id) {
        Trip trip = tripService.getTripById(id);

        List<TripSchedule> schedules = tripScheduleService.getAllSchedulesForTrip(trip);
        List<ScheduleResponseDTO> response = schedules.stream().map(tripScheduleService::mapToDTO).toList();

        return ResponseEntity.ok(
                new ResponseDTO(
                        LocalDateTime.now(),
                        200,
                        "Schedules retrieved successfully",
                        "/api/trips/" + id + "/schedules",
                        response
                )
        );
    }

    @PostMapping
    public ResponseEntity<ResponseDTO> addScheduleToTrip(@PathVariable("id") UUID id, @RequestBody CreateTripScheduleDTO dto) {
        Trip trip = tripService.getTripById(id);

        TripSchedule tripSchedule = tripScheduleService.addScheduleToTrip(trip, dto);

        return ResponseEntity.ok(
                new ResponseDTO(
                        LocalDateTime.now(),
                        200,
                        "Schedule added successfully",
                        "/api/trips/" + id + "/schedules",
                        null
                )
        );
    }

}
