package net.dysky.planner.usersettings;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import net.dysky.planner.auth.JwtService;
import net.dysky.planner.exception.UserNotFoundException;
import net.dysky.planner.response.ResponseDTO;
import net.dysky.planner.user.User;
import net.dysky.planner.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

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

    @PutMapping
    public ResponseEntity<ResponseDTO> updateUserSettings(@RequestBody UpdateSettingsDTO updateSettingsDTO, HttpServletRequest request) {
        String email = jwtService.extractEmail(request);
        User user = userService.getUserByEmail(email);

        if(user == null) {
            throw new UserNotFoundException("User " + email + " not found");
        }

        UserSettings updatedSettings = userSettingsService.updateSettings(user.getSettings(), updateSettingsDTO);

        return ResponseEntity.ok(
                new ResponseDTO(
                        LocalDateTime.now(),
                        200,
                        "User settings updated successfully",
                        "api/users/settings",
                        updatedSettings
                )
        );

    }

}
