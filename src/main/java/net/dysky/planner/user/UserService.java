package net.dysky.planner.user;

import lombok.RequiredArgsConstructor;
import net.dysky.planner.exception.UserNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User findUserById(UUID id) {
        return userRepository.findById(id).orElseThrow(
                () -> new UserNotFoundException("User not found"));
    }

}
