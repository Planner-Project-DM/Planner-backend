package net.dysky.planner.user;

import net.dysky.planner.auth.RegisterDTO;
import net.dysky.planner.exception.UserExistException;
import net.dysky.planner.exception.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldReturnUserWhenUserExists() {
        // given
        UUID id = UUID.randomUUID();
        User mockUser = new User();
        mockUser.setId(id);
        mockUser.setEmail("test@example.com");

        when(userRepository.findById(id)).thenReturn(Optional.of(mockUser));

        // when
        User result = userService.getUserById(id);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getId()).isEqualTo(id);
    }

    @Test
    public void shouldThrowExceptionWhenUserDoesNotExist() {
        // given
        UUID userId = UUID.randomUUID();

        // when
        when(userRepository.findById(userId)).thenThrow(new UserNotFoundException("User not found"));

        // then
        assertThrows(UserNotFoundException.class, () -> userService.getUserById(userId));
    }

    @Test
    void shouldReturnUserWhenUserExistsByEmail() {
        // given
        String email = "test@example.com";
        User mockUser = new User();
        mockUser.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));

        // when
        User result = userService.getUserByEmail(email);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(email);
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotExistByEmail() {
        // given
        String email = "notfound@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // when & then
        assertThrows(UserNotFoundException.class, () -> userService.getUserByEmail(email));
    }

    @Test
    void shouldReturnTrueWhenEmailExists() {
        // given
        String email = "exists@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        // when
        boolean result = userService.existsByEmail(email);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseWhenEmailDoesNotExist() {
        // given
        String email = "new@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(false);

        // when
        boolean result = userService.existsByEmail(email);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void shouldCreateUserSuccessfully() {
        // given
        RegisterDTO dto = new RegisterDTO("John", "Doe", "john.doe@example.com", "123456789", "secretPass");
        when(userRepository.existsByEmail(dto.email())).thenReturn(false);

        User savedUser = new User();
        savedUser.setId(UUID.randomUUID());
        savedUser.setFirstName(dto.firstname());
        savedUser.setLastName(dto.lastName());
        savedUser.setEmail(dto.email());

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // when
        User result = userService.createUser(dto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(dto.email());
        assertThat(result.getFirstName()).isEqualTo(dto.firstname());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenCreatingUserWithExistingEmail() {
        // given
        RegisterDTO dto = new RegisterDTO("John", "Doe", "existing@example.com", "123456789", "secretPass");
        when(userRepository.existsByEmail(dto.email())).thenReturn(true);

        // when & then
        assertThrows(UserExistException.class, () -> userService.createUser(dto));

        verify(userRepository, never()).save(any(User.class));
    }

}
