package net.dysky.planner.friendship;

import net.dysky.planner.exception.UserNotFoundException;
import net.dysky.planner.user.User;
import net.dysky.planner.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FriendshipServiceTest {

    @Mock
    private FriendshipRepository friendshipRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private FriendshipService friendshipService;

    @Test
    void createFriendship_shouldSaveAndReturnFriendship_whenSenderAndReceiverExist() {
        // Given
        String senderEmail = "sender@example.com";
        String receiverEmail = "receiver@example.com";
        CreateFriendshipDTO dto = new CreateFriendshipDTO(senderEmail, receiverEmail);

        User sender = new User();
        sender.setEmail(senderEmail);

        User receiver = new User();
        receiver.setEmail(receiverEmail);

        when(userService.getUserByEmail(senderEmail)).thenReturn(sender);
        when(userService.existsByEmail(receiverEmail)).thenReturn(true);
        when(userService.getUserByEmail(receiverEmail)).thenReturn(receiver);

        when(friendshipRepository.save(any(Friendship.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Friendship result = friendshipService.createFriendship(dto);

        // Then
        assertNotNull(result);
        assertEquals(sender, result.getUserSender());
        assertEquals(receiver, result.getUserReceiver());
        assertEquals(FriendshipStatus.PENDING, result.getStatus());

        verify(userService, times(1)).getUserByEmail(senderEmail);
        verify(userService, times(1)).existsByEmail(receiverEmail);
        verify(userService, times(1)).getUserByEmail(receiverEmail);
        verify(friendshipRepository, times(1)).save(any(Friendship.class));
    }

    @Test
    void createFriendship_shouldThrowUserNotFoundException_whenReceiverDoesNotExist() {
        // Given
        String senderEmail = "sender@example.com";
        String receiverEmail = "nonexistent@example.com";
        CreateFriendshipDTO dto = new CreateFriendshipDTO(senderEmail, receiverEmail);

        User sender = new User();
        sender.setEmail(senderEmail);

        when(userService.getUserByEmail(senderEmail)).thenReturn(sender);
        when(userService.existsByEmail(receiverEmail)).thenReturn(false);

        // When & Then
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> friendshipService.createFriendship(dto));

        assertEquals("User with email nonexistent@example.com not found", exception.getMessage());

        verify(userService, times(1)).getUserByEmail(senderEmail);
        verify(userService, times(1)).existsByEmail(receiverEmail);
        verify(userService, never()).getUserByEmail(receiverEmail);
        verify(friendshipRepository, never()).save(any(Friendship.class));
    }

    @Test
    void getFriendships_shouldReturnListOfDTOs_whereLoggedUserIsSender() {
        // Given
        String loggedUserEmail = "sender@example.com";

        User sender = new User();
        sender.setEmail(loggedUserEmail);
        sender.setFirstName("Jan");
        sender.setLastName("Kowalski");

        User receiver = new User();
        receiver.setEmail("receiver@example.com");
        receiver.setFirstName("Anna");
        receiver.setLastName("Nowak");

        Friendship friendship = new Friendship();
        friendship.setId(UUID.randomUUID());
        friendship.setUserSender(sender);
        friendship.setUserReceiver(receiver);
        friendship.setStatus(FriendshipStatus.PENDING);

        when(userService.getUserByEmail(loggedUserEmail)).thenReturn(sender);
        when(friendshipRepository.findAllByUserFriends(sender)).thenReturn(List.of(friendship));

        // When
        List<FriendshipDTO> result = friendshipService.getFriendships(loggedUserEmail);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());

        FriendshipDTO dto = result.get(0);
        assertEquals("receiver@example.com", dto.email());
        assertEquals("Anna", dto.name());
        assertEquals("Nowak", dto.surname());

        verify(userService, times(1)).getUserByEmail(loggedUserEmail);
        verify(friendshipRepository, times(1)).findAllByUserFriends(sender);
    }

    @Test
    void getFriendships_shouldReturnListOfDTOs_whereLoggedUserIsReceiver() {
        // Given
        String loggedUserEmail = "receiver@example.com";

        User sender = new User();
        sender.setEmail("sender@example.com");
        sender.setFirstName("Jan");
        sender.setLastName("Kowalski");

        User receiver = new User();
        receiver.setEmail(loggedUserEmail);
        receiver.setFirstName("Anna");
        receiver.setLastName("Nowak");

        Friendship friendship = new Friendship();
        friendship.setId(UUID.randomUUID());
        friendship.setUserSender(sender);
        friendship.setUserReceiver(receiver);
        friendship.setStatus(FriendshipStatus.PENDING);

        when(userService.getUserByEmail(loggedUserEmail)).thenReturn(receiver);
        when(friendshipRepository.findAllByUserFriends(receiver)).thenReturn(List.of(friendship));

        // When
        List<FriendshipDTO> result = friendshipService.getFriendships(loggedUserEmail);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());

        FriendshipDTO dto = result.get(0);
        assertEquals("sender@example.com", dto.email());
        assertEquals("Jan", dto.name());
        assertEquals("Kowalski", dto.surname());

        verify(userService, times(1)).getUserByEmail(loggedUserEmail);
        verify(friendshipRepository, times(1)).findAllByUserFriends(receiver);
    }
}