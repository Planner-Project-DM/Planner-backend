package net.dysky.planner.friendship;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import net.dysky.planner.auth.JwtService;
import net.dysky.planner.response.ResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/friendships")
class FriendshipController {

    private final FriendshipService friendshipService;

    private final JwtService jwtService;

    @GetMapping
    public ResponseEntity<ResponseDTO> getFriendships(@RequestParam(required = false) FriendshipStatus status, HttpServletRequest request) {
        String email =  jwtService.extractEmail(request);

        List<FriendshipDTO> friendshipList = (status != null)
                ? friendshipService.getFriendshipsByStatus(email, status)
                : friendshipService.getFriendships(email);

        String requestUrl = (status != null) ? "/api/friendships?status=" + status : "/api/friendships";

        return ResponseEntity.ok(new ResponseDTO(
                LocalDateTime.now(),
                200,
                "Friendships retrieved successfully",
                requestUrl,
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

    @PatchMapping("/{id}/accept")
    public ResponseEntity<ResponseDTO> updateFriendship(@PathVariable("id")UUID id) {
        Friendship friendship = friendshipService.updateStatusFriendship(id, FriendshipStatus.ACCEPTED);

        return ResponseEntity.ok(new ResponseDTO(
                LocalDateTime.now(),
                200,
                "Friendship accepted successfully",
                "/api/friendships/" + id + "/accept",
                friendship
                )
        );
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<ResponseDTO> rejectFriendship(@PathVariable("id")UUID id) {
        Friendship friendship = friendshipService.updateStatusFriendship(id, FriendshipStatus.REJECTED);

        return ResponseEntity.ok(new ResponseDTO(
                LocalDateTime.now(),
                200,
                "Friendship rejected successfully",
                "/api/friendships/" + id + "/reject",
                friendship
                )
        );
    }

    @PatchMapping("/{id}/block")
    public ResponseEntity<ResponseDTO> blockFriendship(@PathVariable("id")UUID id) {
        Friendship friendship = friendshipService.updateStatusFriendship(id, FriendshipStatus.BLOCKED);

        return ResponseEntity.ok(new ResponseDTO(
                        LocalDateTime.now(),
                        200,
                        "Friendship rejected successfully",
                        "/api/friendships/" + id + "/reject",
                        friendship
                )
        );
    }

    @DeleteMapping
    public ResponseEntity<ResponseDTO> deleteFriendship(@RequestBody DeleteFriendshipDTO deleteFriendshipDTO, HttpServletRequest request) {
        String email = jwtService.extractEmail(request);

        friendshipService.deleteFriendship(deleteFriendshipDTO, email);

        return ResponseEntity.ok(new ResponseDTO(
                LocalDateTime.now(),
                200,
                "Friendship deleted successfully",
                "/api/friendships",
                null)
        );
    }

}
