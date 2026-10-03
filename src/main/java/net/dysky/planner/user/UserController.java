package net.dysky.planner.user;

import lombok.RequiredArgsConstructor;
import net.dysky.planner.response.ResponseDTO;
import net.dysky.planner.trip.TripDetailsDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
class UserController {

    private final UserService userService;

    @PutMapping("/{id}")
    public ResponseEntity<ResponseDTO> updateUser(@PathVariable UUID id, @RequestBody UserUpdateDTO dto) {
        User updatedUser = userService.updateUser(id, dto);

        return ResponseEntity.ok(new ResponseDTO(LocalDateTime.now(), 200, "User updated successfully", "api/users/" + id, updatedUser));
    }

}
