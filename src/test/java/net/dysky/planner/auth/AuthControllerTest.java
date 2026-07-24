package net.dysky.planner.auth;

import jakarta.transaction.Transactional;
import net.dysky.planner.AbstractIntegrationTest;
import net.dysky.planner.response.ResponseDTO;
import net.dysky.planner.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
public class AuthControllerTest extends AbstractIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @Test
    @WithMockUser
    void login_ShouldReturnOk_WhenServiceReturnsOk() throws Exception {
        // Given
        LoginDTO loginDTO = new LoginDTO("test@dysky.net", "password123", false);
        ResponseDTO expectedResponse = new ResponseDTO(
                LocalDateTime.now(),
                200,
                "Login successful",
                "auth/login",
                Map.of("token", "mocked-jwt-token")
        );

        // When
        when(authService.login(any(LoginDTO.class)))
                .thenReturn(ResponseEntity.ok(expectedResponse));

        // Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.url").value("auth/login"))
                .andExpect(jsonPath("$.data.token").value("mocked-jwt-token"));
    }

    @Test
    void login_ShouldReturnForbidden_WhenCredentialsAreInvalid() throws Exception {
        // Given
        LoginDTO loginDTO = new LoginDTO("test@dysky.net", "wrong-password", false);
        ResponseDTO expectedResponse = new ResponseDTO(
                LocalDateTime.now(),
                403,
                "Invalid credentials",
                "auth/login",
                null
        );

        when(authService.login(any(LoginDTO.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.FORBIDDEN).body(expectedResponse));

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Invalid credentials"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void register_ShouldReturnOk_WhenRegistrationIsSuccessful() throws Exception {
        // Given
        RegisterDTO registerDTO = new RegisterDTO(
                "Jan",
                "Kowalski",
                "jan@dysky.net",
                "password123",
                "123456789"
        );

        User registeredUser = new User();
        registeredUser.setEmail(registerDTO.email());

        ResponseDTO expectedResponse = new ResponseDTO(
                LocalDateTime.now(),
                200,
                "Register successful",
                "auth/register",
                registeredUser
        );

        when(authService.register(any(RegisterDTO.class)))
                .thenReturn(ResponseEntity.ok(expectedResponse));

        // When & Then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Register successful"))
                .andExpect(jsonPath("$.url").value("auth/register"))
                .andExpect(jsonPath("$.data.email").value("jan@dysky.net"));
    }
}
