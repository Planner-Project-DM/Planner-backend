package net.dysky.planner.auth;

import net.dysky.planner.response.ResponseDTO;
import net.dysky.planner.user.User;
import net.dysky.planner.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserService userService;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private User activeUser;
    private User inactiveUser;

    @BeforeEach
    void setUp() {
        activeUser = new User();
        activeUser.setEmail("test@dysky.net");
        activeUser.setPassword("encoded_password");
        activeUser.setIsActive(true);

        inactiveUser = new User();
        inactiveUser.setEmail("inactive@dysky.net");
        inactiveUser.setPassword("encoded_password");
        inactiveUser.setIsActive(false);
    }

    @Test
    void login_ShouldReturnOk_WhenCredentialsAreValid() {
        // Given
        LoginDTO loginDTO = new LoginDTO("test@dysky.net", "raw_password");
        String mockToken = "mocked-jwt-token";

        when(userService.getUserByEmail(loginDTO.email())).thenReturn(activeUser);
        when(passwordEncoder.matches(loginDTO.password(), activeUser.getPassword())).thenReturn(true);
        when(jwtService.generateToken(activeUser)).thenReturn(mockToken);

        // When
        ResponseEntity<ResponseDTO> responseEntity = authService.login(loginDTO);

        // Then
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(200, responseEntity.getBody().status());
        assertEquals("Login successful", responseEntity.getBody().message());
        assertEquals("auth/login", responseEntity.getBody().url());

        @SuppressWarnings("unchecked")
        Map<String, String> data = (Map<String, String>) responseEntity.getBody().data();
        assertNotNull(data);
        assertEquals(mockToken, data.get("token"));

        verify(userService).getUserByEmail(loginDTO.email());
        verify(passwordEncoder).matches(loginDTO.password(), activeUser.getPassword());
        verify(jwtService).generateToken(activeUser);
    }

    @Test
    void login_ShouldReturnForbidden_WhenUserIsNotActive() {
        // Given
        LoginDTO loginDTO = new LoginDTO("inactive@dysky.net", "raw_password");

        when(userService.getUserByEmail(loginDTO.email())).thenReturn(inactiveUser);

        // When
        ResponseEntity<ResponseDTO> responseEntity = authService.login(loginDTO);

        // Then
        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(403, responseEntity.getBody().status());
        assertEquals("This user is not active", responseEntity.getBody().message());
        assertNull(responseEntity.getBody().data());

        verify(userService).getUserByEmail(loginDTO.email());
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(jwtService);
    }

    @Test
    void login_ShouldReturnForbidden_WhenPasswordIsInvalid() {
        // Given
        LoginDTO loginDTO = new LoginDTO("test@dysky.net", "wrong_password");

        when(userService.getUserByEmail(loginDTO.email())).thenReturn(activeUser);
        when(passwordEncoder.matches(loginDTO.password(), activeUser.getPassword())).thenReturn(false);

        // When
        ResponseEntity<ResponseDTO> responseEntity = authService.login(loginDTO);

        // Then
        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(403, responseEntity.getBody().status());
        assertEquals("Invalid credentials", responseEntity.getBody().message());
        assertNull(responseEntity.getBody().data());

        verify(userService).getUserByEmail(loginDTO.email());
        verify(passwordEncoder).matches(loginDTO.password(), activeUser.getPassword());
        verifyNoInteractions(jwtService);
    }

    @Test
    void register_ShouldReturnOkAndUser_WhenRegistrationIsSuccessful() {
        // Given
        RegisterDTO registerDTO = new RegisterDTO(
                "Jan",
                "Kowalski",
                "jan@dysky.net",
                "raw_password",
                "123456789"
        );
        String encodedPassword = "encoded_password";

        when(passwordEncoder.encode(registerDTO.password())).thenReturn(encodedPassword);

        User createdUser = new User();
        createdUser.setEmail(registerDTO.email());
        when(userService.createUser(any(RegisterDTO.class))).thenReturn(createdUser);

        Map<String, Object> expectedData = new HashMap<>();
        expectedData.put("user", createdUser);
        expectedData.put("token", null);

        // When
        ResponseEntity<ResponseDTO> responseEntity = authService.register(registerDTO);

        // Then
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(200, responseEntity.getBody().status());
        assertEquals("Register successful", responseEntity.getBody().message());
        assertEquals("auth/register", responseEntity.getBody().url());
        assertEquals(expectedData, responseEntity.getBody().data());

        verify(passwordEncoder).encode(registerDTO.password());
        verify(userService).createUser(argThat(dto ->
                dto.email().equals(registerDTO.email()) &&
                        dto.password().equals(encodedPassword)
        ));
    }

    @Test
    void encodePassword_ShouldCallPasswordEncoder() {
        // Given
        String raw = "my_password";
        when(passwordEncoder.encode(raw)).thenReturn("encoded");

        // When
        String result = authService.encodePassword(raw);

        // Then
        assertEquals("encoded", result);
        verify(passwordEncoder).encode(raw);
    }

    @Test
    void verifyPassword_ShouldReturnTrue_WhenPasswordsMatch() {
        // Given
        String raw = "my_password";
        when(passwordEncoder.matches(raw, activeUser.getPassword())).thenReturn(true);

        // When
        boolean result = authService.verifyPassword(raw, activeUser);

        // Then
        assertTrue(result);
        verify(passwordEncoder).matches(raw, activeUser.getPassword());
    }

}
