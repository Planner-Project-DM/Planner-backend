package net.dysky.planner.trip;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import net.dysky.planner.AbstractIntegrationTest;
import net.dysky.planner.auth.JwtService;
import net.dysky.planner.auth.RegisterDTO;
import net.dysky.planner.user.User;
import net.dysky.planner.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
public class TripControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    private User testUser;

    @BeforeEach
    void setUp() {
        when(jwtService.extractEmail(any(HttpServletRequest.class))).thenReturn("user@example.com");

        RegisterDTO registerDTO = new RegisterDTO(
                "Jan",
                "Kowalski",
                "user@example.com",
                "123456789",
                "securePassword123"
        );

        testUser = userService.createUser(registerDTO);
    }

    private Trip createAndSaveTrip(String name, String destination, TripStatus status) {
        Trip trip = new Trip();
        trip.setName(name);
        trip.setDestination(destination);
        trip.setStatus(status);
        trip.setBudget(1500.0);
        trip.setStartDate(LocalDate.now());
        trip.setTripCreator(testUser);
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
                        .with(user("user@example.com").roles("USER"))
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
                        .with(user("user@example.com").roles("USER"))
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
                        .with(user("user@example.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Trip retrieved successfully"))
                .andExpect(jsonPath("$.url").value("/api/trips/" + id))
                .andExpect(jsonPath("$.data.name").value("Rzym 2024"))
                .andExpect(jsonPath("$.createdAt").exists());
    }
}