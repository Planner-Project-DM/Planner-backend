package net.dysky.planner.user;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.dysky.planner.auth.RegisterDTO;
import net.dysky.planner.exception.UserExistException;
import net.dysky.planner.exception.UserNotFoundException;
import net.dysky.planner.usersettings.UserSettingsService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final UserSettingsService userSettingsService;

    private final BCryptPasswordEncoder passwordEncoder;

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
        user.setSettings(userSettingsService.createDefaultSettings());

        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(UUID id, UserUpdateDTO dto) {
        User user = getUserById(id);

        if(dto.firstName() != null) {
            user.setFirstName(dto.firstName());
        }

        if(dto.lastName() != null) {
            user.setLastName(dto.lastName());
        }

        if(dto.email() != null) {
            if(existsByEmail(dto.email()) && !user.getEmail().equals(dto.email())) {
                throw new UserExistException("Email already exists");
            }

            user.setEmail(dto.email());
        }

        if(dto.phoneNumber() != null) {
            user.setPhoneNumber(dto.phoneNumber());
        }

        if(dto.isActive() != user.getIsActive()) {
            user.setIsActive(dto.isActive());
        }

        return user;
    }

    @Transactional
    public void changePassword(String email, String currentPassword, String newPassword) {
        User user = getUserByEmail(email);

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BadCredentialsException("Invalid password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public boolean verifyPassword(String password, User user) {
        return passwordEncoder.matches(password, user.getPassword());
    }


}
