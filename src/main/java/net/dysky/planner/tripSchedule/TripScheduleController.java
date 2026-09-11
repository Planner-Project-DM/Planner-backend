package net.dysky.planner.tripSchedule;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import net.dysky.planner.auth.JwtService;
import net.dysky.planner.response.ResponseDTO;
import net.dysky.planner.schedule.ScheduleResponseDTO;
import net.dysky.planner.trip.Trip;
import net.dysky.planner.trip.TripService;
import net.dysky.planner.user.UserService;
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
    private final JwtService jwtService;

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
    public ResponseEntity<ResponseDTO> addScheduleToTrip(@PathVariable("id") UUID id, @RequestBody CreateTripScheduleDTO dto, HttpServletRequest request) {
        Trip trip = tripService.getTripById(id);
        String email = jwtService.extractEmail(request);

        TripSchedule tripSchedule = tripScheduleService.addScheduleToTrip(trip, dto, email);
        ScheduleResponseDTO response = tripScheduleService.mapToDTO(tripSchedule);

        return ResponseEntity.ok(
                new ResponseDTO(
                        LocalDateTime.now(),
                        200,
                        "Schedule added successfully",
                        "/api/trips/" + id + "/schedules",
                        response
                )
        );
    }

    @PutMapping
    public ResponseEntity<ResponseDTO> updateScheduleInTrip(@PathVariable("id") UUID id, @RequestBody UpdateScheduleDTO dto, HttpServletRequest request) {
        Trip trip = tripService.getTripById(id);
        String email = jwtService.extractEmail(request);

        TripSchedule updated = tripScheduleService.updateScheduleInTrip(trip, dto, email);

        ScheduleResponseDTO response = tripScheduleService.mapToDTO(updated);

        return ResponseEntity.ok(
                new ResponseDTO(
                        LocalDateTime.now(),
                        200,
                        "Schedule updated successfully",
                        "/api/trips/" + id + "/schedules",
                        response
                )
        );
    }

    @DeleteMapping
    public ResponseEntity<ResponseDTO> deleteScheduleFromTrip(@PathVariable("id") UUID id, @RequestParam("scheduleId") UUID scheduleId) {

        tripScheduleService.deleteScheduleFromTrip(id, scheduleId);

        return ResponseEntity.ok(
                new ResponseDTO(
                        LocalDateTime.now(),
                        200,
                        "Schedule deleted successfully",
                        "/api/trips/" + id + "/schedules",
                        null
                )
        );
    }

}
