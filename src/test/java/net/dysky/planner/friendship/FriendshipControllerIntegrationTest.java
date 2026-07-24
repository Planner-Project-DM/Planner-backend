package net.dysky.planner.friendship;

import jakarta.transaction.Transactional;
import net.dysky.planner.AbstractIntegrationTest;
import net.dysky.planner.auth.RegisterDTO;
import net.dysky.planner.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
public class FriendshipControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

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
}