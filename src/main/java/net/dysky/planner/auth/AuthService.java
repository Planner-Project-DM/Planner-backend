package net.dysky.planner.auth;

import lombok.RequiredArgsConstructor;
import net.dysky.planner.response.ResponseDTO;
import net.dysky.planner.user.User;
import net.dysky.planner.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;

    public ResponseEntity<ResponseDTO> login(LoginDTO loginDTO) {
        User user = userService.getUserByEmail(loginDTO.email());

        if(user.getPassword().equals(loginDTO.password())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ResponseDTO(403, "Invalid credentails", null));
        }

        return ResponseEntity.ok(new ResponseDTO(200, "Login successful", user));
    }

    public ResponseEntity<ResponseDTO> register(RegisterDTO registerDTO) {
        if(userService.existsByEmail(registerDTO.email())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResponseDTO(409, "Email already exists", null));
        }

        User user = new User();
        user.setFirstName(registerDTO.firstname());
        user.setLastName(registerDTO.lastName());
        user.setEmail(registerDTO.email());

        // TODO hash
        user.setPassword(registerDTO.password());

        return ResponseEntity.ok(new ResponseDTO(200, "Register successful", user));
    }
}
