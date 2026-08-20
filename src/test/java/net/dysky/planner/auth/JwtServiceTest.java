package net.dysky.planner.auth;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import net.dysky.planner.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtServiceTest {

    private JwtService jwtService;

    private final String testSecretKey = "dGhpcy1pcy1hLXNlY3VyZS1hbmQtc3VmZmljaWVudGx5LWxvbmctc2VjcmV0LWtleS1mb3ItaG1hYy1zaWduaW5n";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", testSecretKey);
    }

    @Test
    void generateToken_shouldCreateTokenCorrectly() {
        User user = mock(User.class);
        when(user.getEmail()).thenReturn("test@domain.com");

        String token = jwtService.generateToken(user, 1000 * 60 * 5);

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractEmail(token)).isEqualTo("test@domain.com");
    }

    @Test
    void extractEmail_withRequest_shouldReturnEmailWhenHeaderIsValid() {
        User user = mock(User.class);
        when(user.getEmail()).thenReturn("user@domain.com");
        String token = jwtService.generateToken(user, 1000 * 60);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        String email = jwtService.extractEmail(request);

        assertThat(email).isEqualTo("user@domain.com");
    }

    @Test
    void extractEmail_withRequest_shouldThrowExceptionWhenHeaderIsNull() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn(null);

        assertThatThrownBy(() -> jwtService.extractEmail(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid Authorization header");
    }

    @Test
    void extractEmail_withRequest_shouldThrowExceptionWhenHeaderDoesNotStartWithBearer() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Basic dGVzdDp0ZXN0");

        assertThatThrownBy(() -> jwtService.extractEmail(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid Authorization header");
    }

    @Test
    void isTokenValid_shouldReturnTrueWhenEmailMatchesAndTokenNotExpired() {
        User user = mock(User.class);
        when(user.getEmail()).thenReturn("user@domain.com");
        String token = jwtService.generateToken(user, 1000 * 60);

        boolean isValid = jwtService.isTokenValid(token, user);

        assertThat(isValid).isTrue();
    }

    @Test
    void isTokenValid_shouldReturnFalseWhenEmailDoesNotMatch() {
        User user = mock(User.class);
        when(user.getEmail()).thenReturn("user@domain.com");
        String token = jwtService.generateToken(user, 1000 * 60);

        User otherUser = mock(User.class);
        when(otherUser.getEmail()).thenReturn("other@domain.com");

        boolean isValid = jwtService.isTokenValid(token, otherUser);

        assertThat(isValid).isFalse();
    }

    @Test
    void isTokenValid_shouldThrowExpiredJwtExceptionWhenTokenHasExpired() throws InterruptedException {
        User user = mock(User.class);
        when(user.getEmail()).thenReturn("user@domain.com");

        String token = jwtService.generateToken(user, 1);

        Thread.sleep(5);

        assertThatThrownBy(() -> jwtService.isTokenValid(token, user))
                .isInstanceOf(ExpiredJwtException.class);
    }
}