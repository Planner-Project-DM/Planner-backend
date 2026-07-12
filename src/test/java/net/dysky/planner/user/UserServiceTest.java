package net.dysky.planner.user;

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
import static org.mockito.Mockito.when;

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

}
