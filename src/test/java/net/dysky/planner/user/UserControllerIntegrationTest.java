package net.dysky.planner.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import net.dysky.planner.AbstractIntegrationTest;
import net.dysky.planner.auth.JwtService;
import net.dysky.planner.auth.RegisterDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
public class UserControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtService jwtService;

    private User testUser;
    private final String userEmail = "integration_user@example.com";

    @BeforeEach
    void setUp() {
        when(jwtService.extractEmail(any(HttpServletRequest.class))).thenReturn(userEmail);

        RegisterDTO registerDTO = new RegisterDTO(
                "John",
                "Doe",
                userEmail,
                passwordEncoder.encode("Password123!"),
                "123456789"
        );
        testUser = userService.createUser(registerDTO);
    }

    @Test
    @DisplayName("PUT: updateUser_shouldReturnOkAndUpdatedUser_whenValidDataProvided")
    void updateUser_shouldReturnOkAndUpdatedUser_whenValidDataProvided() throws Exception {
        UserUpdateDTO updateDTO = new UserUpdateDTO(
                "Johnny",
                "Smith",
                "updated_email@example.com",
                "987654321",
                true
        );

        mockMvc.perform(put("/api/users/{id}", testUser.getId())
                        .with(user(userEmail).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("User updated successfully"))
                .andExpect(jsonPath("$.url").value("api/users/" + testUser.getId()))
                .andExpect(jsonPath("$.data.firstName").value("Johnny"))
                .andExpect(jsonPath("$.data.lastName").value("Smith"))
                .andExpect(jsonPath("$.data.email").value("updated_email@example.com"))
                .andExpect(jsonPath("$.data.phoneNumber").value("987654321"))
                .andExpect(jsonPath("$.data.isActive").value(true));
    }

    @Test
    @DisplayName("PUT: updateUser_shouldReturnNotFound_whenUserDoesNotExist")
    void updateUser_shouldReturnNotFound_whenUserDoesNotExist() throws Exception {
        UUID nonExistingId = UUID.randomUUID();
        UserUpdateDTO updateDTO = new UserUpdateDTO(
                "Ghost",
                "User",
                "ghost@example.com",
                "000000000",
                true
        );

        mockMvc.perform(put("/api/users/{id}", nonExistingId)
                        .with(user(userEmail).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT: updateUser_shouldReturnConflict_whenEmailAlreadyExists")
    void updateUser_shouldReturnConflict_whenEmailAlreadyExists() throws Exception {
        String existingEmail = "second_user@example.com";
        RegisterDTO secondUserDTO = new RegisterDTO(
                "Jane",
                "Doe",
                existingEmail,
                "555555555",
                passwordEncoder.encode("Password123!")
        );
        userService.createUser(secondUserDTO);

        UserUpdateDTO updateDTO = new UserUpdateDTO(
                "John",
                "Doe",
                existingEmail,
                "123456789",
                true
        );

        mockMvc.perform(put("/api/users/{id}", testUser.getId())
                        .with(user(userEmail).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PUT: changePassword_shouldReturnOk_whenCredentialsAreValid")
    void changePassword_shouldReturnOk_whenCredentialsAreValid() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("Password123!", "NewPassword123!");

        mockMvc.perform(put("/api/users/password")
                        .with(user(userEmail).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Password changed successfully"))
                .andExpect(jsonPath("$.url").value("api/users/me/password"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }
}