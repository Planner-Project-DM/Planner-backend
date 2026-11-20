package net.dysky.planner.trip;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import net.dysky.planner.AbstractIntegrationTest;
import net.dysky.planner.auth.JwtService;
import net.dysky.planner.auth.RegisterDTO;
import net.dysky.planner.group.CreateGroupDTO;
import net.dysky.planner.group.GroupService;
import net.dysky.planner.user.User;
import net.dysky.planner.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class TripNotesControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String OWNER = "owner@example.com";

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtService jwtService;

    private Trip trip;

    @BeforeEach
    void setUp() {
        when(jwtService.extractEmail(any(HttpServletRequest.class))).thenReturn(OWNER);

        User owner = userService.createUser(new RegisterDTO("Jan", "Kowalski", OWNER, "123456789", "securePassword123"));

        Trip t = new Trip();
        t.setName("Notes trip");
        t.setDestination("Rome");
        t.setStatus(TripStatus.PLANNED);
        t.setBudget(1500.0);
        t.setStartDate(LocalDate.now());
        t.setEndDate(LocalDate.now().plusDays(5));
        t.setTripCreator(owner);
        t.setTripGroup(groupService.createGroup(new CreateGroupDTO(""), OWNER));
        trip = tripRepository.saveAndFlush(t);
    }

    private String body(String content, Long version) {
        Map<String, Object> map = new java.util.HashMap<>();
        map.put("content", content);
        map.put("version", version);
        return objectMapper.writeValueAsString(map);
    }

    private void putNotes(UUID id, String content, Long version) throws Exception {
        mockMvcPut(id, body(content, version)).andExpect(status().isOk());
    }

    private org.springframework.test.web.servlet.ResultActions mockMvcPut(UUID id, String json) throws Exception {
        return mockMvc.perform(put("/api/trips/{id}/notes", id)
                .with(user(OWNER).roles("USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    @Test
    void getNotes_shouldReturnNullContentAndVersionZero_whenNothingSaved() throws Exception {
        mockMvc.perform(get("/api/trips/{id}/notes", trip.getId())
                        .with(user(OWNER).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.url").value("/api/trips/" + trip.getId() + "/notes"))
                .andExpect(jsonPath("$.data.content").doesNotExist())
                .andExpect(jsonPath("$.data.version").value(0));
    }

    @Test
    void saveNotes_shouldPersistAndGetShouldReturnThem() throws Exception {
        mockMvcPut(trip.getId(), body("{\"shapes\":[]}", null))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Trip notes saved successfully"))
                .andExpect(jsonPath("$.data.content").value("{\"shapes\":[]}"))
                .andExpect(jsonPath("$.data.version").value(0));

        mockMvc.perform(get("/api/trips/{id}/notes", trip.getId())
                        .with(user(OWNER).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").value("{\"shapes\":[]}"))
                .andExpect(jsonPath("$.data.version").value(0));
    }

    @Test
    void saveNotes_shouldReturnBadRequest_whenContentIsNotJson() throws Exception {
        mockMvcPut(trip.getId(), body("{not json", null))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void saveNotes_shouldReturnBadRequest_whenContentIsBlank() throws Exception {
        mockMvcPut(trip.getId(), body("", null))
                .andExpect(status().isBadRequest());
    }

    @Test
    void saveNotes_shouldReturnConflict_whenVersionIsStale() throws Exception {
        putNotes(trip.getId(), "{\"v\":1}", null);

        mockMvcPut(trip.getId(), body("{\"v\":2}", 99L))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void saveNotes_shouldReturnPayloadTooLarge_whenContentExceedsLimit() throws Exception {
        String huge = "\"" + "x".repeat(5 * 1024 * 1024) + "\"";

        mockMvcPut(trip.getId(), body(huge, null))
                .andExpect(status().isContentTooLarge())
                .andExpect(jsonPath("$.status").value(413));
    }

    @Test
    void notes_shouldReturnForbidden_whenUserIsNotInTripGroup() throws Exception {
        userService.createUser(new RegisterDTO("Adam", "Nowak", "outsider@example.com", "987654321", "securePassword456"));

        mockMvc.perform(get("/api/trips/{id}/notes", trip.getId())
                        .with(user("outsider@example.com").roles("USER")))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/trips/{id}/notes", trip.getId())
                        .with(user("outsider@example.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("{}", null)))
                .andExpect(status().isForbidden());
    }

    @Test
    void notes_shouldReturnNotFound_whenTripDoesNotExist() throws Exception {
        UUID randomId = UUID.randomUUID();

        mockMvc.perform(get("/api/trips/{id}/notes", randomId)
                        .with(user(OWNER).roles("USER")))
                .andExpect(status().isNotFound());

        mockMvcPut(randomId, body("{}", null))
                .andExpect(status().isNotFound());
    }

    @Test
    void notes_shouldReject_whenNoAuthentication() throws Exception {
        mockMvc.perform(get("/api/trips/{id}/notes", trip.getId()))
                .andExpect(status().is4xxClientError());
    }
}
