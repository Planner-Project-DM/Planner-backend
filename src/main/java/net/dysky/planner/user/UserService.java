package net.dysky.planner.user;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.dysky.planner.auth.RegisterDTO;
import net.dysky.planner.exception.UserExistException;
import net.dysky.planner.exception.UserNotFoundException;
import net.dysky.planner.setings.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final SettingsService settingsService;

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new UserNotFoundException("User not found"));
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public User getUserById(UUID id) {
        return userRepository.findById(id).orElseThrow(
                () -> new UserNotFoundException("User not found"));
    }

    @Transactional
    public User createUser(RegisterDTO registerDTO) {

        if(existsByEmail(registerDTO.email())) {
            throw new UserExistException("Email already exists");
        }

        User user = new User();
        user.setFirstName(registerDTO.firstName());
        user.setLastName(registerDTO.lastName());
        user.setEmail(registerDTO.email());
        user.setPhoneNumber(registerDTO.phoneNumber());

        user.setPassword(registerDTO.password());
        user.setSettings(settingsService.createDefaultSettings());

        return userRepository.save(user);
    }

}
