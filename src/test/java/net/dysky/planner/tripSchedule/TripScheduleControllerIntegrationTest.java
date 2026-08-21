package net.dysky.planner.tripSchedule;

import net.dysky.planner.AbstractIntegrationTest;
import net.dysky.planner.exception.TripNotFoundException;
import net.dysky.planner.schedule.ScheduleResponseDTO;
import net.dysky.planner.schedule.TripItemSummaryDTO;
import net.dysky.planner.trip.Trip;
import net.dysky.planner.trip.TripService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class TripScheduleControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TripService tripService;

    @MockitoBean
    private TripScheduleService tripScheduleService;

    @Test
    void getAllSchedulesForTrip_shouldReturnSchedules_whenTripExists() throws Exception {
        // Given
        UUID tripId = UUID.randomUUID();
        Trip mockTrip = new Trip();
        mockTrip.setId(tripId);

        TripSchedule mockTripSchedule = new TripSchedule();
        ScheduleResponseDTO mockDto = new ScheduleResponseDTO(
                UUID.randomUUID(),
                new TripItemSummaryDTO(UUID.randomUUID(), "Hotel Marriott"),
                "2026-08-02T10:00",
                "2026-08-02T12:00"
        );

        when(tripService.getTripById(tripId)).thenReturn(mockTrip);
        when(tripScheduleService.getAllSchedulesForTrip(mockTrip)).thenReturn(List.of(mockTripSchedule));
        when(tripScheduleService.mapToDTO(mockTripSchedule)).thenReturn(mockDto);

        // When & Then
        mockMvc.perform(get("/api/trips/{id}/schedules", tripId)
                        .with(user("user@example.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Schedules retrieved successfully"))
                .andExpect(jsonPath("$.url").value("/api/trips/" + tripId + "/schedules"))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].tripItem.name").value("Hotel Marriott"));

        verify(tripService, times(1)).getTripById(tripId);
        verify(tripScheduleService, times(1)).getAllSchedulesForTrip(mockTrip);
    }

    @Test
    void getAllSchedulesForTrip_shouldReturnNotFound_whenTripDoesNotExist() throws Exception {
        // Given
        UUID tripId = UUID.randomUUID();
        when(tripService.getTripById(tripId)).thenThrow(new TripNotFoundException("Trip not found"));

        // When & Then
        mockMvc.perform(get("/api/trips/{id}/schedules", tripId)
                        .with(user("user@example.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Trip not found"));

        verify(tripService, times(1)).getTripById(tripId);
        verifyNoInteractions(tripScheduleService);
    }

    @Test
    void addScheduleToTrip_shouldReturnOk_whenRequestIsValid() throws Exception {
        // Given
        UUID tripId = UUID.randomUUID();
        Trip mockTrip = new Trip();
        mockTrip.setId(tripId);

        CreateTripScheduleDTO dto = new CreateTripScheduleDTO(
                UUID.randomUUID(),
                LocalDateTime.of(2026, 8, 2, 10, 0),
                LocalDateTime.of(2026, 8, 2, 12, 0)
        );

        when(tripService.getTripById(tripId)).thenReturn(mockTrip);
        when(tripScheduleService.addScheduleToTrip(eq(mockTrip), any(CreateTripScheduleDTO.class)))
                .thenReturn(new TripSchedule());

        // When & Then
        mockMvc.perform(post("/api/trips/{id}/schedules", tripId)
                        .with(user("user@example.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Schedule added successfully"));

        verify(tripService, times(1)).getTripById(tripId);
        verify(tripScheduleService, times(1)).addScheduleToTrip(eq(mockTrip), any(CreateTripScheduleDTO.class));
    }

    @Test
    void addScheduleToTrip_shouldReturnNotFound_whenTripDoesNotExist() throws Exception {
        // Given
        UUID tripId = UUID.randomUUID();
        CreateTripScheduleDTO dto = new CreateTripScheduleDTO(UUID.randomUUID(), LocalDateTime.now(), LocalDateTime.now());

        when(tripService.getTripById(tripId)).thenThrow(new TripNotFoundException("Trip not found"));

        // When & Then
        mockMvc.perform(post("/api/trips/{id}/schedules", tripId)
                        .with(user("user@example.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(tripService, times(1)).getTripById(tripId);
        verifyNoMoreInteractions(tripScheduleService);
    }

    @Test
    void addScheduleToTrip_shouldReturnBadRequest_whenScheduleOverlaps() throws Exception {
        // Given
        UUID tripId = UUID.randomUUID();
        Trip mockTrip = new Trip();
        mockTrip.setId(tripId);

        CreateTripScheduleDTO dto = new CreateTripScheduleDTO(UUID.randomUUID(), LocalDateTime.now(), LocalDateTime.now());

        when(tripService.getTripById(tripId)).thenReturn(mockTrip);
        when(tripScheduleService.addScheduleToTrip(eq(mockTrip), any(CreateTripScheduleDTO.class)))
                .thenThrow(new IllegalArgumentException("Schedule overlaps with an existing schedule"));

        // When & Then
        mockMvc.perform(post("/api/trips/{id}/schedules", tripId)
                        .with(user("user@example.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Schedule overlaps with an existing schedule"));

        verify(tripService, times(1)).getTripById(tripId);
        verify(tripScheduleService, times(1)).addScheduleToTrip(eq(mockTrip), any(CreateTripScheduleDTO.class));
    }

    @Test
    void deleteScheduleFromTrip_shouldReturnOk_whenSuccessfullyDeleted() throws Exception {
        // Given
        UUID tripId = UUID.randomUUID();
        UUID scheduleId = UUID.randomUUID();

        // When & Then
        mockMvc.perform(delete("/api/trips/{id}/schedules", tripId)
                        .param("scheduleId", scheduleId.toString())
                        .with(user("user@example.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Schedule deleted successfully"));

        verify(tripScheduleService, times(1)).deleteScheduleFromTrip(tripId, scheduleId);
    }

    @Test
    void deleteScheduleFromTrip_shouldReturnBadRequest_whenScheduleDoesNotExistForTrip() throws Exception {
        // Given
        UUID tripId = UUID.randomUUID();
        UUID scheduleId = UUID.randomUUID();

        doThrow(new IllegalArgumentException("Schedule not found for the given trip"))
                .when(tripScheduleService).deleteScheduleFromTrip(tripId, scheduleId);

        // When & Then
        mockMvc.perform(delete("/api/trips/{id}/schedules", tripId)
                        .param("scheduleId", scheduleId.toString())
                        .with(user("user@example.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Schedule not found for the given trip"));

        verify(tripScheduleService, times(1)).deleteScheduleFromTrip(tripId, scheduleId);
    }
}