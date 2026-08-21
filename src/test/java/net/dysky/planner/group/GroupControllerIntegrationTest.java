package net.dysky.planner.group;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import net.dysky.planner.AbstractIntegrationTest;
import net.dysky.planner.auth.JwtService;
import net.dysky.planner.auth.RegisterDTO;
import net.dysky.planner.groupUser.CreateGroupUserDTO;
import net.dysky.planner.groupUser.GroupRole;
import net.dysky.planner.groupUser.GroupUser;
import net.dysky.planner.groupUser.GroupUserService;
import net.dysky.planner.trip.Trip;
import net.dysky.planner.trip.TripService;
import net.dysky.planner.user.User;
import net.dysky.planner.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
public class GroupControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupUserService groupUserService;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private TripService tripService;

    private User testUser;

    @BeforeEach
    void setUp() {
        when(jwtService.extractEmail(any(HttpServletRequest.class))).thenReturn("user@example.com");

        RegisterDTO registerDTO = new RegisterDTO(
                "Jan",
                "Kowalski",
                "user@example.com",
                "123456789",
                "securePassword123"
        );
        testUser = userService.createUser(registerDTO);
    }

    private Group createAndSaveGroup(String groupName) {
        Group group = new Group();
        group.setName(groupName);
        return groupRepository.save(group);
    }

    private User createAndSaveSecondaryUser(String email) {
        RegisterDTO registerDTO = new RegisterDTO(
                "Adam",
                "Nowak",
                email,
                "987654321",
                "securePassword456"
        );
        return userService.createUser(registerDTO);
    }

    @Test
    void addToGroup_shouldAddUserSuccessfully_whenUserExistsAndIsNotInGroup() throws Exception {
        UUID tripId = UUID.randomUUID();
        Group group = createAndSaveGroup("Paryż Group");
        createAndSaveSecondaryUser("secondary@example.com");

        Trip mockTrip = mock(Trip.class);
        when(tripService.getTripById(tripId)).thenReturn(mockTrip);
        when(mockTrip.getTripGroup()).thenReturn(group);

        AddToGroupDTO dto = new AddToGroupDTO("secondary@example.com");

        mockMvc.perform(post("/api/trips/{id}/group/members", tripId)
                        .with(user("user@example.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("User added to group successfully"))
                .andExpect(jsonPath("$.url").value("/api/groups/add"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void addToGroup_shouldReturnBadRequest_whenUserIsAlreadyInGroup() throws Exception {
        UUID tripId = UUID.randomUUID();
        Group group = createAndSaveGroup("Paryż Group");
        User secondaryUser = createAndSaveSecondaryUser("secondary@example.com");

        groupUserService.add(new CreateGroupUserDTO(group, secondaryUser, GroupRole.MEMBER));

        GroupUser mockGroupUser = mock(GroupUser.class);
        when(mockGroupUser.getUser()).thenReturn(secondaryUser);
        group.getGroupUsers().add(mockGroupUser);

        Trip mockTrip = mock(Trip.class);
        when(tripService.getTripById(tripId)).thenReturn(mockTrip);
        when(mockTrip.getTripGroup()).thenReturn(group);

        AddToGroupDTO dto = new AddToGroupDTO("secondary@example.com");

        mockMvc.perform(post("/api/trips/{id}/group/members", tripId)
                        .with(user("user@example.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("User is already in the group"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void removeFromGroup_shouldRemoveUserSuccessfully_whenUserIsInGroup() throws Exception {
        UUID tripId = UUID.randomUUID();
        Group group = createAndSaveGroup("Paryż Group");
        User secondaryUser = createAndSaveSecondaryUser("secondary@example.com");

        groupUserService.add(new CreateGroupUserDTO(group, secondaryUser, GroupRole.MEMBER));

        GroupUser mockGroupUser = mock(GroupUser.class);
        when(mockGroupUser.getUser()).thenReturn(secondaryUser);
        group.getGroupUsers().add(mockGroupUser);

        Trip mockTrip = mock(Trip.class);
        when(tripService.getTripById(tripId)).thenReturn(mockTrip);
        when(mockTrip.getTripGroup()).thenReturn(group);

        RemoveFromGroupDTO dto = new RemoveFromGroupDTO("Adam", "secondary@example.com");

        mockMvc.perform(delete("/api/trips/{id}/group/members", tripId)
                        .with(user("user@example.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("User removed from group successfully"))
                .andExpect(jsonPath("$.url").value("/api/groups/remove"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void removeFromGroup_shouldReturnBadRequest_whenUserIsNotInGroup() throws Exception {
        UUID tripId = UUID.randomUUID();
        Group group = createAndSaveGroup("Paryż Group");
        createAndSaveSecondaryUser("secondary@example.com");

        Trip mockTrip = mock(Trip.class);
        when(tripService.getTripById(tripId)).thenReturn(mockTrip);
        when(mockTrip.getTripGroup()).thenReturn(group);

        RemoveFromGroupDTO dto = new RemoveFromGroupDTO("Adam", "secondary@example.com");

        mockMvc.perform(delete("/api/trips/{id}/group/members", tripId)
                        .with(user("user@example.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("User is not in the group"))
                .andExpect(jsonPath("$.createdAt").exists());
    }
}