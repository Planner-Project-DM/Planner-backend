package net.dysky.planner.trip;

import jakarta.transaction.Transactional;
import net.dysky.planner.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
public class TripControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TripRepository tripRepository;

    private Trip createAndSaveTrip(String name, String destination, TripStatus status) {
        Trip trip = new Trip();
        trip.setName(name);
        trip.setDestination(destination);
        trip.setStatus(status);
        trip.setBudget(1500.0);
        trip.setStartDate(LocalDate.now());
        trip.setEndDate(LocalDate.now().plusDays(5));
        return tripRepository.saveAndFlush(trip);
    }

    @Test
    void getTrips_shouldReturnAllTrips_whenStatusIsNotProvided() throws Exception {
        // Given
        createAndSaveTrip("Paryż 2024", "Paryż", TripStatus.PLANNED);
        createAndSaveTrip("Londyn 2024", "Londyn", TripStatus.COMPLETED);

        // When & Then
        mockMvc.perform(get("/api/trips")
                        .with(user("user").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Trips retrieved successfully"))
                .andExpect(jsonPath("$.url").value("/api/trips"))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void getTrips_shouldReturnFilteredTrips_whenStatusIsProvided() throws Exception {
        // Given
        createAndSaveTrip("Paryż 2024", "Paryż", TripStatus.PLANNED);
        createAndSaveTrip("Londyn 2024", "Londyn", TripStatus.COMPLETED);

        // When & Then
        mockMvc.perform(get("/api/trips")
                        .with(user("user").roles("USER"))
                        .param("status", "PLANNED")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Trips retrieved successfully"))
                .andExpect(jsonPath("$.url").value("/api/trips?status=PLANNED"))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Paryż 2024"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void getTripById_shouldReturnTrip_whenIdIsValid() throws Exception {
        // Given
        Trip savedTrip = createAndSaveTrip("Rzym 2024", "Rzym", TripStatus.PLANNED);
        UUID id = savedTrip.getId();

        // When & Then
        mockMvc.perform(get("/api/trips/{id}", id)
                        .with(user("user").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Trip retrieved successfully"))
                .andExpect(jsonPath("$.url").value("/api/trips/" + id))
                .andExpect(jsonPath("$.data.name").value("Rzym 2024"))
                .andExpect(jsonPath("$.createdAt").exists());
    }
}