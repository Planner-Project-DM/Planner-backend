package net.dysky.planner.auth;

import lombok.RequiredArgsConstructor;
import net.dysky.planner.response.ResponseDTO;
import net.dysky.planner.user.User;
import net.dysky.planner.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;

    public ResponseEntity<ResponseDTO> login(LoginDTO loginDTO) {
        User user = userService.getUserByEmail(loginDTO.email());

        if(!user.getPassword().equals(loginDTO.password())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ResponseDTO(LocalDateTime.now(), 403, "Invalid credentails", "auth/login", null));
        }

        return ResponseEntity.ok(new ResponseDTO(LocalDateTime.now(), 200, "Login successful", "auth/login", user));
    }

    public ResponseEntity<ResponseDTO> register(RegisterDTO registerDTO) {
        User user = userService.createUser(registerDTO);

        return ResponseEntity.ok(new ResponseDTO(LocalDateTime.now(), 200, "Register successful", "auth/register", user));
    }
}
