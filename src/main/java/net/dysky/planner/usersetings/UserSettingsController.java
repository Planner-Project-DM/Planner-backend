package net.dysky.planner.usersetings;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import net.dysky.planner.auth.JwtService;
import net.dysky.planner.exception.UserNotFoundException;
import net.dysky.planner.response.ResponseDTO;
import net.dysky.planner.user.User;
import net.dysky.planner.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/settings")
public class UserSettingsController {

    private final UserSettingsService userSettingsService;

    private final JwtService jwtService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<ResponseDTO> getUserSettings(HttpServletRequest request) {
        String email = jwtService.extractEmail(request);
        User user = userService.getUserByEmail(email);

        if(user == null) {
            throw new UserNotFoundException("User " + email + " not found");
        }

        UserSettings settings = user.getSettings();

        return ResponseEntity.ok(
                new ResponseDTO(
                        LocalDateTime.now(),
                        200,
                        "User settings retrieved successfully",
                        "api/users/settings",
                        settings
                )
        );
    }

}
