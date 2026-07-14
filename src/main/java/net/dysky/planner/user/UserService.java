package net.dysky.planner.user;

import jakarta.transaction.Transactional;
import net.dysky.planner.auth.RegisterDTO;
import net.dysky.planner.exception.UserExistException;
import net.dysky.planner.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

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

        // TODO hash
        user.setPassword(registerDTO.password());

        return userRepository.save(user);
    }

}
