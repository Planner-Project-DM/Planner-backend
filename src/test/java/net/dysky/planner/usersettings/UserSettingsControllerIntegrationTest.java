package net.dysky.planner.usersettings;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import net.dysky.planner.AbstractIntegrationTest;
import net.dysky.planner.auth.JwtService;
import net.dysky.planner.auth.RegisterDTO;
import net.dysky.planner.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
public class UserSettingsControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtService jwtService;

    private final String userEmail = "settings_user@example.com";

    @BeforeEach
    void setUp() {
        when(jwtService.extractEmail(any(HttpServletRequest.class))).thenReturn(userEmail);

        RegisterDTO registerDTO = new RegisterDTO(
                "Piotr",
                "Nowak",
                userEmail,
                "987654321",
                "Password123!"
        );
        userService.createUser(registerDTO);
    }

    @Test
    @DisplayName("GET: shouldReturnSettings_whenUserExists")
    void getUserSettings_shouldReturnSettings_whenUserExists() throws Exception {
        mockMvc.perform(get("/api/users/settings")
                        .with(user(userEmail).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("User settings retrieved successfully"))
                .andExpect(jsonPath("$.url").value("api/users/settings"))
                .andExpect(jsonPath("$.data.currency").value("PLN"))
                .andExpect(jsonPath("$.data.language").value("PL"))
                .andExpect(jsonPath("$.data.budgetLimit").value(0.0))
                .andExpect(jsonPath("$.data.notificationEnabled").value(true));
    }

    @Test
    @DisplayName("GET: shouldReturnNotFound_whenUserDoesNotExist")
    void getUserSettings_shouldReturnNotFound_whenUserDoesNotExist() throws Exception {
        String nonExistingEmail = "ghost@example.com";
        when(jwtService.extractEmail(any(HttpServletRequest.class))).thenReturn(nonExistingEmail);

        mockMvc.perform(get("/api/users/settings")
                        .with(user(nonExistingEmail).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT: shouldUpdateAllFields_whenAllFieldsInDtoAreProvided")
    void updateUserSettings_shouldUpdateAllFields_whenAllFieldsInDtoAreProvided() throws Exception {
        UpdateSettingsDTO updateDto = new UpdateSettingsDTO(
                "EUR",
                4500.0,
                "EN",
                false
        );

        mockMvc.perform(put("/api/users/settings")
                        .with(user(userEmail).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("User settings updated successfully"))
                .andExpect(jsonPath("$.url").value("api/users/settings"))
                .andExpect(jsonPath("$.data.currency").value("EUR"))
                .andExpect(jsonPath("$.data.budgetLimit").value(4500.0))
                .andExpect(jsonPath("$.data.language").value("EN"))
                .andExpect(jsonPath("$.data.notificationEnabled").value(false));
    }

    @Test
    @DisplayName("PUT: shouldNotChangeFields_whenAllFieldsInDtoAreNull")
    void updateUserSettings_shouldNotChangeFields_whenAllFieldsInDtoAreNull() throws Exception {
        UpdateSettingsDTO emptyDto = new UpdateSettingsDTO(null, null, null, null);

        mockMvc.perform(put("/api/users/settings")
                        .with(user(userEmail).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("User settings updated successfully"))
                .andExpect(jsonPath("$.url").value("api/users/settings"))
                .andExpect(jsonPath("$.data.currency").value("PLN"))
                .andExpect(jsonPath("$.data.budgetLimit").value(0.0))
                .andExpect(jsonPath("$.data.language").value("PL"))
                .andExpect(jsonPath("$.data.notificationEnabled").value(true));
    }

    @Test
    @DisplayName("PUT: shouldReturnNotFound_whenUserDoesNotExist")
    void updateUserSettings_shouldReturnNotFound_whenUserDoesNotExist() throws Exception {
        String nonExistingEmail = "ghost@example.com";
        when(jwtService.extractEmail(any(HttpServletRequest.class))).thenReturn(nonExistingEmail);

        UpdateSettingsDTO updateDto = new UpdateSettingsDTO("USD", 1000.0, "EN", true);

        mockMvc.perform(put("/api/users/settings")
                        .with(user(nonExistingEmail).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());
    }
}