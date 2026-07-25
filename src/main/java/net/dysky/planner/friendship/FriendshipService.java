package net.dysky.planner.friendship;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.dysky.planner.exception.UserNotFoundException;
import net.dysky.planner.user.User;
import net.dysky.planner.user.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;

    private final UserService userService;

    public List<FriendshipDTO> getFriendships(String email) {
        User user = userService.getUserByEmail(email);

        List<Friendship> friendships = friendshipRepository.findAllByUserFriends(user);

        return friendships.stream().map(friendship -> mapToFriendshipDTO(friendship, email)).toList();
    }

    @Transactional
    public Friendship createFriendship(CreateFriendshipDTO createFriendshipDTO) {
        User sender = userService.getUserByEmail(createFriendshipDTO.emailSender());

        if(!userService.existsByEmail(createFriendshipDTO.emailReceiver())) {
            throw new UserNotFoundException("User with email " + createFriendshipDTO.emailReceiver() + " not found");
        }

        User receiver = userService.getUserByEmail(createFriendshipDTO.emailReceiver());

        Friendship friendship = new Friendship();

        friendship.setUserSender(sender);
        friendship.setUserReceiver(receiver);

        friendship.setStatus(FriendshipStatus.PENDING);

        return friendshipRepository.save(friendship);
    }

    FriendshipDTO mapToFriendshipDTO(Friendship friendship, String email) {
        User user = friendship.getUserSender().getEmail().equals(email) ? friendship.getUserReceiver() : friendship.getUserSender();

        return new FriendshipDTO(
                friendship.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName()
        );
    }


}
