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
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
public class TripControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

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
        createAndSaveTrip("Paryż 2024", "Paryż", TripStatus.PLANNED);
        createAndSaveTrip("Londyn 2024", "Londyn", TripStatus.COMPLETED);

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
        createAndSaveTrip("Paryż 2024", "Paryż", TripStatus.PLANNED);
        createAndSaveTrip("Londyn 2024", "Londyn", TripStatus.COMPLETED);

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
        Trip savedTrip = createAndSaveTrip("Rzym 2024", "Rzym", TripStatus.PLANNED);
        UUID id = savedTrip.getId();

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

    @Test
    void getTripById_shouldReturnNotFound_whenTripDoesNotExist() throws Exception {
        UUID randomId = UUID.randomUUID();

        mockMvc.perform(get("/api/trips/{id}", randomId)
                        .with(user("user@example.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Trip not found"))
                .andExpect(jsonPath("$.url").value("/api/trips/" + randomId))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void createTrip_shouldReturnInternalServerError_whenBudgetIsZeroOrNegative() throws Exception {
        CreateTripDTO invalidDto = new CreateTripDTO("Wycieczka", "Rzym", -100.0, LocalDate.now(), LocalDate.now().plusDays(5));

        mockMvc.perform(post("/api/trips")
                        .with(user("user@example.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Budget must be a positive value"))
                .andExpect(jsonPath("$.url").value("/api/trips"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void createTrip_shouldReturnBadRequest_whenDataIsInvalid() throws Exception {
        CreateTripDTO invalidDto = new CreateTripDTO("", "", -100.0, null, null);

        mockMvc.perform(post("/api/trips")
                        .with(user("user@example.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTrip_shouldReturnConflict_whenTripNameAlreadyExistsForUser() throws Exception {
        createAndSaveTrip("Rzym 2024", "Rzym", TripStatus.PLANNED);

        CreateTripDTO duplicateDto = new CreateTripDTO("Rzym 2024", "Włochy", 3000.0, LocalDate.now(), LocalDate.now().plusDays(5));

        mockMvc.perform(post("/api/trips")
                        .with(user("user@example.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Trip with the same name already exists for this user."))
                .andExpect(jsonPath("$.url").value("/api/trips"));
    }

    @Test
    void updateTrip_shouldPartiallyUpdateTrip_whenDataIsValid() throws Exception {
        Trip savedTrip = createAndSaveTrip("Rzym 2024", "Rzym", TripStatus.PLANNED);
        UUID id = savedTrip.getId();

        UpdateTripDTO updateDto = new UpdateTripDTO("Włochy 2024", null, null, 2000.0, null, null, null);

        mockMvc.perform(patch("/api/trips/{id}", id)
                        .with(user("user@example.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Trip updated successfully"))
                .andExpect(jsonPath("$.data.name").value("Włochy 2024"))
                .andExpect(jsonPath("$.data.destination").value("Rzym"))
                .andExpect(jsonPath("$.data.budget").value(2000.0))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void updateTrip_shouldReturnNotFound_whenTripDoesNotExist() throws Exception {
        UUID randomId = UUID.randomUUID();
        UpdateTripDTO updateDto = new UpdateTripDTO("Nowa Nazwa", null, null, null, null, null, null);

        mockMvc.perform(patch("/api/trips/{id}", randomId)
                        .with(user("user@example.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void deleteTrip_shouldDeleteSuccessfully_whenTripExists() throws Exception {
        Trip savedTrip = createAndSaveTrip("Rzym 2024", "Rzym", TripStatus.PLANNED);
        UUID id = savedTrip.getId();

        mockMvc.perform(delete("/api/trips/{id}", id)
                        .with(user("user@example.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Trip deleted successfully"));

        assertFalse(tripRepository.findById(id).isPresent());
    }

    @Test
    void deleteTrip_shouldReturnNotFound_whenTripDoesNotExist() throws Exception {
        UUID randomId = UUID.randomUUID();

        mockMvc.perform(delete("/api/trips/{id}", randomId)
                        .with(user("user@example.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}