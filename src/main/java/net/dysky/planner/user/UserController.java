package net.dysky.planner.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.dysky.planner.auth.JwtService;
import net.dysky.planner.response.ResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
class UserController {

    private final UserService userService;

    private final JwtService jwtService;

    @GetMapping("/me")
    public ResponseEntity<ResponseDTO> getUserInformation(HttpServletRequest request) {
        String email = jwtService.extractEmail(request);

        User user = userService.getUserByEmail(email);

        return ResponseEntity.ok(new ResponseDTO(LocalDateTime.now(), 200, "User information get successfully", "/api/users/me", user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseDTO> updateUser(@PathVariable UUID id, @RequestBody UserUpdateDTO dto) {
        User updatedUser = userService.updateUser(id, dto);

        return ResponseEntity.ok(new ResponseDTO(LocalDateTime.now(), 200, "User updated successfully", "api/users/" + id, updatedUser));
    }

    @PutMapping("/password")
    public ResponseEntity<ResponseDTO> changePassword(
            HttpServletRequest request,
            @Valid @RequestBody ChangePasswordRequest dto
    ) {
        String email = jwtService.extractEmail(request);

        userService.changePassword(email, dto.currentPassword(), dto.newPassword());

        return ResponseEntity.ok(new ResponseDTO(LocalDateTime.now(), 200, "Password changed successfully", "api/users/me/password", null));
    }

}
