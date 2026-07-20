package net.dysky.planner.friendship;

import lombok.RequiredArgsConstructor;
import net.dysky.planner.response.ResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/friendships")
class FriendshipController {

    private final FriendshipService friendshipService;

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
