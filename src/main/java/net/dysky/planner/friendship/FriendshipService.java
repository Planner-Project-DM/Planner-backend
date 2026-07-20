package net.dysky.planner.friendship;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.dysky.planner.exception.UserNotFoundException;
import net.dysky.planner.user.User;
import net.dysky.planner.user.UserService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;

    private final UserService userService;

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



}
