package net.dysky.planner.friendship;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.dysky.planner.exception.FriendshipExistsException;
import net.dysky.planner.exception.FriendshipNotFoundException;
import net.dysky.planner.exception.UserNotFoundException;
import net.dysky.planner.notification.NotificationService;
import net.dysky.planner.user.User;
import net.dysky.planner.user.UserService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;

    private final UserService userService;

    private final NotificationService notificationService;

    public List<FriendshipDTO> getFriendships(String email) {
        User user = userService.getUserByEmail(email);

        List<Friendship> friendships = friendshipRepository.findAllByUserFriends(user);

        return friendships.stream().map(friendship -> mapToFriendshipDTO(friendship, email)).toList();
    }

    public List<FriendshipDTO> getFriendshipsByStatus(String email, FriendshipStatus status) {
        User user = userService.getUserByEmail(email);
        List<Friendship> friendships = friendshipRepository.findAllByUserFriendsAndStatus(user, status);

        return friendships.stream().map(friendship -> mapToFriendshipDTO(friendship, email)).toList();
    }

    public Friendship getFriendship(User provider, User friend) {
        return friendshipRepository.findFriendshipBy(provider, friend)
                .orElseThrow(() -> new FriendshipNotFoundException("Friendship not found"));
    }

    public List<FriendshipDTO> getMyFriendshipRequest(String email) {
        List<Friendship> friendships = friendshipRepository.findByUserReceiver_EmailAndStatus(email, FriendshipStatus.PENDING);
        return friendships.stream().map(friendship -> mapToFriendshipDTO(friendship, email)).toList();

    }

    public Friendship findById(UUID id) {
        return friendshipRepository.findById(id).orElseThrow(
                () -> new FriendshipNotFoundException("Friendship with id " + id + " not found"));
    }

    @Transactional
    public Friendship createFriendship(CreateFriendshipDTO createFriendshipDTO) {
        User sender = userService.getUserByEmail(createFriendshipDTO.emailSender());

        if(!userService.existsByEmail(createFriendshipDTO.emailReceiver())) {
            throw new UserNotFoundException("User with email " + createFriendshipDTO.emailReceiver() + " not found");
        }

        User receiver = userService.getUserByEmail(createFriendshipDTO.emailReceiver());

        if(sender.getEmail().equals(receiver.getEmail())) {
            throw new FriendshipExistsException("You cannot send a friendship request to yourself");
        }

        if(friendshipRepository.existsFriendshipBy(sender, receiver)) {
            throw new FriendshipExistsException("Friendship already exists");
        }

        Friendship friendship = new Friendship();

        friendship.setUserSender(sender);
        friendship.setUserReceiver(receiver);

        friendship.setStatus(FriendshipStatus.PENDING);

        notificationService.createNotification(
                "Friendship request from " + sender.getFirstName() + " " + sender.getLastName(),
                " You have a new friendship request from " + sender.getFirstName() + " " + sender.getLastName(),
                receiver.getId(),
                sender.getId()
        );

        return friendshipRepository.save(friendship);
    }

    @Transactional
    public Friendship updateStatusFriendship(UUID id, FriendshipStatus status) {
        Friendship friendship = findById(id);
        friendship.setStatus(status);
        return friendshipRepository.save(friendship);
    }

    @Transactional
    public void deleteFriendship(DeleteFriendshipDTO deleteFriendshipDTO, String email) {
        User user = userService.getUserByEmail(email);

        User friend = userService.getUserByEmail(deleteFriendshipDTO.email());

        Friendship friendship =  getFriendship(user, friend);
        friendshipRepository.delete(friendship);

        notificationService.createNotification(
                "Friendship removed",
                "You have removed " + friend.getFirstName() + " " + friend.getLastName() + " from your friends list",
                user.getId()
        );

        notificationService.createNotification(
                "Friendship removed",
                "You are no longer friends with " + user.getFirstName() + " " + user.getLastName(),
                friend.getId(),
                user.getId()
        );
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
