package net.dysky.planner.friendship;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import net.dysky.planner.auth.JwtService;
import net.dysky.planner.response.ResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/friendships")
class FriendshipController {

    private final FriendshipService friendshipService;

    private final JwtService jwtService;

    @GetMapping
    public ResponseEntity<ResponseDTO> getFriendships(HttpServletRequest request) {
        String email =  jwtService.extractEmail(request);

        List<FriendshipDTO> friendshipList = friendshipService.getFriendships(email);

        return ResponseEntity.ok(new ResponseDTO(
                LocalDateTime.now(),
                200,
                "Friendships retrieved successfully",
                "/api/friendships",
                friendshipList)
        );
    }

    @PostMapping("/create")
    public ResponseEntity<ResponseDTO> createFriendship(@RequestBody CreateFriendshipDTO createFriendshipDTO) {
        Friendship friendship = friendshipService.createFriendship(createFriendshipDTO);

        return ResponseEntity.ok(new ResponseDTO(
                LocalDateTime.now(),
                200,
                "Send request to " + friendship.getUserReceiver().getEmail(),
                "/api/friendships/create",
                null)
        );
    }

}
