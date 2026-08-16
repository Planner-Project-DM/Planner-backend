package net.dysky.planner.tripitinerary;

import net.dysky.planner.AbstractIntegrationTest;
import net.dysky.planner.trip.Trip;
import net.dysky.planner.trip.TripService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class TripItineraryControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TripService tripService;

    @MockitoBean
    private TripItineraryService tripItineraryService;

    @Test
    void addTripItem_shouldReturnOk_whenSuccessfullyAdded() throws Exception {
        // Given
        UUID tripId = UUID.randomUUID();
        UUID tripItemId = UUID.randomUUID();

        CreateTripItineraryDTO dto = new CreateTripItineraryDTO(tripItemId);

        Trip mockTrip = new Trip();
        mockTrip.setId(tripId);

        when(tripService.getTripById(tripId)).thenReturn(mockTrip);

        // When & Then
        mockMvc.perform(post("/api/trips/{id}/item", tripId)
                        .with(user("user@example.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Trip item added successfully"))
                .andExpect(jsonPath("$.url").value("/api/trips/" + tripId + "/item"))
                .andExpect(jsonPath("$.createdAt").exists());

        verify(tripService).getTripById(tripId);
        verify(tripItineraryService).addTripItinerary(eq(mockTrip), any(CreateTripItineraryDTO.class));
        verify(tripService).updateTripCosts(tripId);
    }

    @Test
    void updateTripItem_shouldReturnOk_whenSuccessfullyUpdated() throws Exception {
        // Given
        UUID tripId = UUID.randomUUID();
        UUID tripItemId = UUID.randomUUID();

        UpdateTripItineraryDTO dto = new UpdateTripItineraryDTO(tripItemId, 150.0);

        Trip mockTrip = new Trip();
        mockTrip.setId(tripId);

        when(tripService.getTripById(tripId)).thenReturn(mockTrip);

        // When & Then
        mockMvc.perform(put("/api/trips/{id}/item", tripId)
                        .with(user("user@example.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Trip item updated successfully"))
                .andExpect(jsonPath("$.url").value("/api/trips/" + tripId + "/item"))
                .andExpect(jsonPath("$.createdAt").exists());

        verify(tripService).getTripById(tripId);
        verify(tripItineraryService).updateTripItinerary(eq(mockTrip), any(UpdateTripItineraryDTO.class));
        verify(tripService).updateTripCosts(tripId);
    }

    @Test
    void deleteTripItem_shouldReturnOk_whenSuccessfullyDeleted() throws Exception {
        UUID tripId = UUID.randomUUID();
        UUID tripItemId = UUID.randomUUID();

        mockMvc.perform(delete("/api/trips/{id}/item", tripId)
                        .param("tripItemId", tripItemId.toString())
                        .with(user("user@example.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Trip item deleted successfully"))
                .andExpect(jsonPath("$.url").value("/api/trips/" + tripId + "/item"))
                .andExpect(jsonPath("$.createdAt").exists());

        verify(tripItineraryService).deleteTripItinerary(tripId, tripItemId);
        verify(tripService).updateTripCosts(tripId);
    }
}