package net.dysky.planner.friendship;

import net.dysky.planner.exception.FriendshipExistsException;
import net.dysky.planner.exception.FriendshipNotFoundException;
import net.dysky.planner.exception.UserNotFoundException;
import net.dysky.planner.user.User;
import net.dysky.planner.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
        when(friendshipRepository.existsFriendshipBy(sender, receiver)).thenReturn(false);

        when(friendshipRepository.save(any(Friendship.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Friendship result = friendshipService.createFriendship(dto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserSender()).isEqualTo(sender);
        assertThat(result.getUserReceiver()).isEqualTo(receiver);
        assertThat(result.getStatus()).isEqualTo(FriendshipStatus.PENDING);

        verify(userService).getUserByEmail(senderEmail);
        verify(userService).existsByEmail(receiverEmail);
        verify(userService).getUserByEmail(receiverEmail);
        verify(friendshipRepository).save(any(Friendship.class));
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
        assertThatThrownBy(() -> friendshipService.createFriendship(dto))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User with email nonexistent@example.com not found");

        verify(userService).getUserByEmail(senderEmail);
        verify(userService).existsByEmail(receiverEmail);
        verifyNoMoreInteractions(userService);
        verifyNoInteractions(friendshipRepository);
    }

    @Test
    void createFriendship_shouldThrowFriendshipExistsException_whenSenderEmailEqualsReceiverEmail() {
        // Given
        String email = "user@example.com";
        CreateFriendshipDTO dto = new CreateFriendshipDTO(email, email);

        User sender = new User();
        sender.setEmail(email);

        when(userService.getUserByEmail(email)).thenReturn(sender);
        when(userService.existsByEmail(email)).thenReturn(true);
        when(userService.getUserByEmail(email)).thenReturn(sender);

        // When & Then
        assertThatThrownBy(() -> friendshipService.createFriendship(dto))
                .isInstanceOf(FriendshipExistsException.class)
                .hasMessageContaining("You cannot send a friendship request to yourself");

        verify(friendshipRepository, never()).save(any(Friendship.class));
    }

    @Test
    void createFriendship_shouldThrowFriendshipExistsException_whenFriendshipAlreadyExists() {
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
        when(friendshipRepository.existsFriendshipBy(sender, receiver)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> friendshipService.createFriendship(dto))
                .isInstanceOf(FriendshipExistsException.class)
                .hasMessageContaining("Friendship already exists");

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
        assertThat(result).hasSize(1);
        FriendshipDTO dto = result.get(0);
        assertThat(dto.email()).isEqualTo("receiver@example.com");
        assertThat(dto.name()).isEqualTo("Anna");
        assertThat(dto.surname()).isEqualTo("Nowak");
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
        assertThat(result).hasSize(1);
        FriendshipDTO dto = result.get(0);
        assertThat(dto.email()).isEqualTo("sender@example.com");
        assertThat(dto.name()).isEqualTo("Jan");
        assertThat(dto.surname()).isEqualTo("Kowalski");
    }

    @Test
    void getFriendshipsByStatus_shouldReturnFilteredFriendships() {
        // Given
        String email = "user@example.com";
        User user = new User();
        user.setEmail(email);

        User friend = new User();
        friend.setEmail("friend@example.com");
        friend.setFirstName("Adam");
        friend.setLastName("Nowak");

        Friendship friendship = new Friendship();
        friendship.setId(UUID.randomUUID());
        friendship.setUserSender(user);
        friendship.setUserReceiver(friend);
        friendship.setStatus(FriendshipStatus.ACCEPTED);

        when(userService.getUserByEmail(email)).thenReturn(user);
        when(friendshipRepository.findAllByUserFriendsAndStatus(user, FriendshipStatus.ACCEPTED))
                .thenReturn(List.of(friendship));

        // When
        List<FriendshipDTO> result = friendshipService.getFriendshipsByStatus(email, FriendshipStatus.ACCEPTED);

        // Then
        assertThat(result).hasSize(1);
        FriendshipDTO dto = result.get(0);
        assertThat(dto.email()).isEqualTo("friend@example.com");
        assertThat(dto.name()).isEqualTo("Adam");
        assertThat(dto.surname()).isEqualTo("Nowak");
    }

    @Test
    void getMyFriendshipRequest_shouldReturnIncomingPendingRequests() {
        // Given
        String email = "receiver@example.com";
        User sender = new User();
        sender.setEmail("sender@example.com");
        sender.setFirstName("Robert");
        sender.setLastName("Kowal");

        User receiver = new User();
        receiver.setEmail(email);

        Friendship friendship = new Friendship();
        friendship.setId(UUID.randomUUID());
        friendship.setUserSender(sender);
        friendship.setUserReceiver(receiver);
        friendship.setStatus(FriendshipStatus.PENDING);

        when(friendshipRepository.findByUserReceiver_EmailAndStatus(email, FriendshipStatus.PENDING))
                .thenReturn(List.of(friendship));

        // When
        List<FriendshipDTO> result = friendshipService.getMyFriendshipRequest(email);

        // Then
        assertThat(result).hasSize(1);
        FriendshipDTO dto = result.get(0);
        assertThat(dto.email()).isEqualTo("sender@example.com");
        assertThat(dto.name()).isEqualTo("Robert");
        assertThat(dto.surname()).isEqualTo("Kowal");
    }

    @Test
    void findById_shouldReturnFriendshipWhenExists() {
        // Given
        UUID id = UUID.randomUUID();
        Friendship friendship = new Friendship();
        friendship.setId(id);

        when(friendshipRepository.findById(id)).thenReturn(Optional.of(friendship));

        // When
        Friendship result = friendshipService.findById(id);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(id);
    }

    @Test
    void findById_shouldThrowFriendshipNotFoundExceptionWhenDoesNotExist() {
        // Given
        UUID id = UUID.randomUUID();
        when(friendshipRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> friendshipService.findById(id))
                .isInstanceOf(FriendshipNotFoundException.class)
                .hasMessageContaining("Friendship with id " + id + " not found");
    }

    @Test
    void updateStatusFriendship_shouldUpdateStatusAndSave() {
        // Given
        UUID id = UUID.randomUUID();
        Friendship friendship = new Friendship();
        friendship.setId(id);
        friendship.setStatus(FriendshipStatus.PENDING);

        when(friendshipRepository.findById(id)).thenReturn(Optional.of(friendship));
        when(friendshipRepository.save(any(Friendship.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Friendship result = friendshipService.updateStatusFriendship(id, FriendshipStatus.ACCEPTED);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(FriendshipStatus.ACCEPTED);
        verify(friendshipRepository).save(friendship);
    }

    @Test
    void deleteFriendship_shouldDeleteSuccessfully_whenFriendshipExists() {
        // Given
        String userEmail = "user@example.com";
        String friendEmail = "friend@example.com";
        DeleteFriendshipDTO dto = new DeleteFriendshipDTO(friendEmail);

        User user = new User();
        user.setEmail(userEmail);

        User friend = new User();
        friend.setEmail(friendEmail);

        Friendship friendship = new Friendship();
        friendship.setUserSender(user);
        friendship.setUserReceiver(friend);

        when(userService.getUserByEmail(userEmail)).thenReturn(user);
        when(userService.getUserByEmail(friendEmail)).thenReturn(friend);
        when(friendshipRepository.findFriendshipBy(user, friend)).thenReturn(Optional.of(friendship));

        // When
        friendshipService.deleteFriendship(dto, userEmail);

        // Then
        verify(friendshipRepository).delete(friendship);
        verify(userService).getUserByEmail(userEmail);
        verify(userService).getUserByEmail(friendEmail);
        verify(friendshipRepository).findFriendshipBy(user, friend);
    }

    @Test
    void deleteFriendship_shouldThrowFriendshipNotFoundException_whenFriendshipDoesNotExist() {
        // Given
        String userEmail = "user@example.com";
        String friendEmail = "friend@example.com";
        DeleteFriendshipDTO dto = new DeleteFriendshipDTO(friendEmail);

        User user = new User();
        user.setEmail(userEmail);

        User friend = new User();
        friend.setEmail(friendEmail);

        when(userService.getUserByEmail(userEmail)).thenReturn(user);
        when(userService.getUserByEmail(friendEmail)).thenReturn(friend);
        when(friendshipRepository.findFriendshipBy(user, friend)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> friendshipService.deleteFriendship(dto, userEmail))
                .isInstanceOf(FriendshipNotFoundException.class)
                .hasMessageContaining("Friendship not found");

        verify(friendshipRepository, never()).delete(any(Friendship.class));
        verify(userService).getUserByEmail(userEmail);
        verify(userService).getUserByEmail(friendEmail);
    }
}