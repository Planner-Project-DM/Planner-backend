package net.dysky.planner.auth;

import lombok.RequiredArgsConstructor;
import net.dysky.planner.response.ResponseDTO;
import net.dysky.planner.user.User;
import net.dysky.planner.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtService jwtService;
    private final UserService userService;
    private final BCryptPasswordEncoder passwordEncoder;

    public ResponseEntity<ResponseDTO> login(LoginDTO loginDTO) {
        User user = userService.getUserByEmail(loginDTO.email());

        if (!user.getIsActive()) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new ResponseDTO(
                            LocalDateTime.now(),
                            403,
                            "This user is not active",
                            "auth/login",
                            null)
                    );
        }

        if(!verifyPassword(loginDTO.password(), user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ResponseDTO(LocalDateTime.now(), 403, "Invalid credentials", "auth/login", null));
        }

        String token = jwtService.generateToken(user);

        Map<String, String> response = new HashMap<>();
        response.put("token", token);

        return ResponseEntity.ok(new ResponseDTO(LocalDateTime.now(), 200, "Login successful", "auth/login", response));
    }

    public ResponseEntity<ResponseDTO> register(RegisterDTO registerDTO) {
        RegisterDTO registerDTOWithEncodedPassword = new RegisterDTO(
                registerDTO.firstName(),
                registerDTO.lastName(),
                registerDTO.email(),
                encodePassword(registerDTO.password()),
                registerDTO.phoneNumber()
        );

        User user = userService.createUser(registerDTOWithEncodedPassword);

        return ResponseEntity.ok(new ResponseDTO(LocalDateTime.now(), 200, "Register successful", "auth/register", user));
    }

    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public boolean verifyPassword(String password, User user) {
        return passwordEncoder.matches(password, user.getPassword());
    }
}
