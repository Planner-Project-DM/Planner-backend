package net.dysky.planner.friendship;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import net.dysky.planner.AbstractIntegrationTest;
import net.dysky.planner.auth.JwtService;
import net.dysky.planner.auth.RegisterDTO;
import net.dysky.planner.notification.NotificationService;
import net.dysky.planner.user.User;
import net.dysky.planner.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
public class FriendshipControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private NotificationService notificationService;

    @Autowired
    private FriendshipService friendshipService;

    @Autowired
    private FriendshipRepository friendshipRepository;

    private void registerUser(String firstName, String lastName, String email) {
        RegisterDTO registerDTO = new RegisterDTO(firstName, lastName, email, "123456789", "password123");
        userService.createUser(registerDTO);
    }

    @Test
    void createFriendship_shouldReturnOk_whenRequestIsValid() throws Exception {
        // Given
        String senderEmail = "sender@example.com";
        String receiverEmail = "receiver@example.com";

        registerUser("Jan", "Kowalski", senderEmail);
        registerUser("Anna", "Nowak", receiverEmail);

        CreateFriendshipDTO dto = new CreateFriendshipDTO(senderEmail, receiverEmail);

        // When & Then
        mockMvc.perform(post("/api/friendships/create")
                        .with(user(senderEmail).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Send request to " + receiverEmail))
                .andExpect(jsonPath("$.url").value("/api/friendships/create"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void createFriendship_shouldReturnNotFound_whenReceiverDoesNotExist() throws Exception {
        // Given
        String senderEmail = "sender@example.com";
        String nonexistentEmail = "nonexistent@example.com";

        registerUser("Jan", "Kowalski", senderEmail);

        CreateFriendshipDTO dto = new CreateFriendshipDTO(senderEmail, nonexistentEmail);

        // When & Then
        mockMvc.perform(post("/api/friendships/create")
                        .with(user(senderEmail).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("User with email " + nonexistentEmail + " not found"))
                .andExpect(jsonPath("$.url").value("/api/friendships/create"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void getFriendships_shouldReturnListOfFriendships_whenUserIsAuthenticated() throws Exception {
        // Given
        String senderEmail = "sender@example.com";
        String receiverEmail = "receiver@example.com";

        registerUser("Jan", "Kowalski", senderEmail);
        registerUser("Anna", "Nowak", receiverEmail);

        User sender = userService.getUserByEmail(senderEmail);
        User receiver = userService.getUserByEmail(receiverEmail);

        Friendship friendship = new Friendship();
        friendship.setUserSender(sender);
        friendship.setUserReceiver(receiver);
        friendship.setStatus(FriendshipStatus.ACCEPTED);
        friendshipRepository.saveAndFlush(friendship);

        when(jwtService.extractEmail(any(HttpServletRequest.class))).thenReturn(senderEmail);

        // When & Then
        mockMvc.perform(get("/api/friendships")
                        .with(user(senderEmail).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Friendships retrieved successfully"))
                .andExpect(jsonPath("$.url").value("/api/friendships"))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].email").value(receiverEmail))
                .andExpect(jsonPath("$.data[0].name").value("Anna"))
                .andExpect(jsonPath("$.data[0].surname").value("Nowak"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void getFriendships_shouldReturnFilteredFriendships_whenStatusParameterIsProvided() throws Exception {
        // Given
        String senderEmail = "sender@example.com";
        String receiverEmail = "receiver@example.com";

        registerUser("Jan", "Kowalski", senderEmail);
        registerUser("Anna", "Nowak", receiverEmail);

        User sender = userService.getUserByEmail(senderEmail);
        User receiver = userService.getUserByEmail(receiverEmail);

        Friendship friendship = new Friendship();
        friendship.setUserSender(sender);
        friendship.setUserReceiver(receiver);
        friendship.setStatus(FriendshipStatus.ACCEPTED);
        friendshipRepository.saveAndFlush(friendship);

        when(jwtService.extractEmail(any(HttpServletRequest.class))).thenReturn(senderEmail);

        // When & Then
        mockMvc.perform(get("/api/friendships")
                        .with(user(senderEmail).roles("USER"))
                        .param("status", "ACCEPTED")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.url").value("/api/friendships?status=ACCEPTED"))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].email").value(receiverEmail));
    }

    @Test
    void getMyFriendshipRequests_shouldReturnPendingRequests_whenRequested() throws Exception {
        // Given
        String senderEmail = "sender@example.com";
        String receiverEmail = "receiver@example.com";

        registerUser("Jan", "Kowalski", senderEmail);
        registerUser("Anna", "Nowak", receiverEmail);

        User sender = userService.getUserByEmail(senderEmail);
        User receiver = userService.getUserByEmail(receiverEmail);

        Friendship friendship = new Friendship();
        friendship.setUserSender(sender);
        friendship.setUserReceiver(receiver);
        friendship.setStatus(FriendshipStatus.PENDING);
        friendshipRepository.saveAndFlush(friendship);

        when(jwtService.extractEmail(any(HttpServletRequest.class))).thenReturn(receiverEmail);

        // When & Then
        mockMvc.perform(get("/api/friendships/my-requests")
                        .with(user(receiverEmail).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Friendship requests retrieved successfully"))
                .andExpect(jsonPath("$.url").value("/api/friendships/my-requests"))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].email").value(senderEmail));
    }

    @Test
    void updateFriendship_shouldAcceptFriendship_whenIdIsValid() throws Exception {
        // Given
        String senderEmail = "sender@example.com";
        String receiverEmail = "receiver@example.com";

        registerUser("Jan", "Kowalski", senderEmail);
        registerUser("Anna", "Nowak", receiverEmail);

        User sender = userService.getUserByEmail(senderEmail);
        User receiver = userService.getUserByEmail(receiverEmail);

        Friendship friendship = new Friendship();
        friendship.setUserSender(sender);
        friendship.setUserReceiver(receiver);
        friendship.setStatus(FriendshipStatus.PENDING);
        friendship = friendshipRepository.saveAndFlush(friendship);

        UUID id = friendship.getId();

        // When & Then
        mockMvc.perform(patch("/api/friendships/{id}/accept", id)
                        .with(user(receiverEmail).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Friendship accepted successfully"))
                .andExpect(jsonPath("$.url").value("/api/friendships/" + id + "/accept"))
                .andExpect(jsonPath("$.data.status").value("ACCEPTED"));
    }

    @Test
    void rejectFriendship_shouldRejectFriendship_whenIdIsValid() throws Exception {
        // Given
        String senderEmail = "sender@example.com";
        String receiverEmail = "receiver@example.com";

        registerUser("Jan", "Kowalski", senderEmail);
        registerUser("Anna", "Nowak", receiverEmail);

        User sender = userService.getUserByEmail(senderEmail);
        User receiver = userService.getUserByEmail(receiverEmail);

        Friendship friendship = new Friendship();
        friendship.setUserSender(sender);
        friendship.setUserReceiver(receiver);
        friendship.setStatus(FriendshipStatus.PENDING);
        friendship = friendshipRepository.saveAndFlush(friendship);

        UUID id = friendship.getId();

        // When & Then
        mockMvc.perform(patch("/api/friendships/{id}/reject", id)
                        .with(user(receiverEmail).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Friendship rejected successfully"))
                .andExpect(jsonPath("$.url").value("/api/friendships/" + id + "/reject"))
                .andExpect(jsonPath("$.data.status").value("REJECTED"));
    }

    @Test
    void blockFriendship_shouldBlockFriendship_whenIdIsValid() throws Exception {
        // Given
        String senderEmail = "sender@example.com";
        String receiverEmail = "receiver@example.com";

        registerUser("Jan", "Kowalski", senderEmail);
        registerUser("Anna", "Nowak", receiverEmail);

        User sender = userService.getUserByEmail(senderEmail);
        User receiver = userService.getUserByEmail(receiverEmail);

        Friendship friendship = new Friendship();
        friendship.setUserSender(sender);
        friendship.setUserReceiver(receiver);
        friendship.setStatus(FriendshipStatus.PENDING);
        friendship = friendshipRepository.saveAndFlush(friendship);

        UUID id = friendship.getId();

        // When & Then
        mockMvc.perform(patch("/api/friendships/{id}/block", id)
                        .with(user(receiverEmail).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Friendship rejected successfully"))
                .andExpect(jsonPath("$.url").value("/api/friendships/" + id + "/reject"))
                .andExpect(jsonPath("$.data.status").value("BLOCKED"));
    }

    @Test
    void deleteFriendship_shouldReturnOk_whenFriendshipIsDeletedSuccessfully() throws Exception {
        // Given
        registerUser("SYSTEM", "SYSTEM", "system@planner.net");

        String userEmail = "user@example.com";
        String friendEmail = "friend@example.com";

        registerUser("Jan", "Kowalski", userEmail);
        registerUser("Anna", "Nowak", friendEmail);

        User user = userService.getUserByEmail(userEmail);
        User friend = userService.getUserByEmail(friendEmail);

        Friendship friendship = new Friendship();
        friendship.setUserSender(user);
        friendship.setUserReceiver(friend);
        friendship.setStatus(FriendshipStatus.ACCEPTED);
        friendshipRepository.saveAndFlush(friendship);

        DeleteFriendshipDTO dto = new DeleteFriendshipDTO(friendEmail);

        when(jwtService.extractEmail(any(HttpServletRequest.class))).thenReturn(userEmail);

        // When & Then
        mockMvc.perform(delete("/api/friendships")
                        .with(user(userEmail).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Friendship deleted successfully"))
                .andExpect(jsonPath("$.url").value("/api/friendships"))
                .andExpect(jsonPath("$.createdAt").exists());

        assertTrue(friendshipRepository.findFriendshipBy(user, friend).isEmpty());
    }

    @Test
    void deleteFriendship_shouldReturnNotFound_whenFriendshipDoesNotExist() throws Exception {
        // Given
        String userEmail = "user@example.com";
        String friendEmail = "friend@example.com";

        registerUser("Jan", "Kowalski", userEmail);
        registerUser("Anna", "Nowak", friendEmail);

        DeleteFriendshipDTO dto = new DeleteFriendshipDTO(friendEmail);

        when(jwtService.extractEmail(any(HttpServletRequest.class))).thenReturn(userEmail);

        // When & Then
        mockMvc.perform(delete("/api/friendships")
                        .with(user(userEmail).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.url").value("/api/friendships"))
                .andExpect(jsonPath("$.createdAt").exists());
    }
}