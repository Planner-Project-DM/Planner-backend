package net.dysky.planner.group;

import net.dysky.planner.groupUser.CreateGroupUserDTO;
import net.dysky.planner.groupUser.GroupUser;
import net.dysky.planner.groupUser.GroupUserService;
import net.dysky.planner.notification.NotificationService;
import net.dysky.planner.user.User;
import net.dysky.planner.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import net.dysky.planner.groupUser.GroupRole;
import net.dysky.planner.groupUser.UpdateGroupUserDTO;
import net.dysky.planner.trip.Trip;
import net.dysky.planner.exception.UserNotFoundException;
import org.mockito.ArgumentCaptor;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private GroupUserService groupUserService;

    @Mock
    private UserService userService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private GroupService groupService;

    @Test
    void findByName_shouldReturnGroupWhenExists() {
        Group group = new Group();
        group.setName("Summer Trip");

        when(groupRepository.findByName("Summer Trip")).thenReturn(Optional.of(group));

        Group result = groupService.findByName("Summer Trip");

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Summer Trip");
        verify(groupRepository).findByName("Summer Trip");
    }

    @Test
    void findByName_shouldThrowExceptionWhenGroupDoesNotExist() {
        when(groupRepository.findByName("NonExistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> groupService.findByName("NonExistent"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Group not found");
    }

    @Test
    void createGroup_shouldSaveGroupAndAddOwner() {
        CreateGroupDTO dto = new CreateGroupDTO("Adventure Team");
        String ownerEmail = "owner@domain.com";

        Group group = new Group();
        group.setName("Adventure Team");

        User owner = mock(User.class);

        when(groupRepository.save(any(Group.class))).thenReturn(group);
        when(userService.getUserByEmail(ownerEmail)).thenReturn(owner);

        Group result = groupService.createGroup(dto, ownerEmail);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Adventure Team");

        verify(groupRepository).save(any(Group.class));
        verify(groupUserService).add(any(CreateGroupUserDTO.class));
    }

    @Test
    void addToGroup_shouldAddUserWhenNotAlreadyInGroup() {
        Group group = new Group();
        group.setGroupUsers(new ArrayList<>());
        User senderUser = mock(User.class);

        when(userService.getUserByEmail("sender@domain.com")).thenReturn(senderUser);

        AddToGroupDTO dto = new AddToGroupDTO("newmember@domain.com");
        User userToAdd = mock(User.class);

        when(userService.getUserByEmail("newmember@domain.com")).thenReturn(userToAdd);

        groupService.addToGroup(group, dto, "sender@domain.com");

        verify(userService).getUserByEmail("sender@domain.com");
        verify(userService).getUserByEmail("newmember@domain.com");
        verify(groupUserService).add(any(CreateGroupUserDTO.class));
    }

    @Test
    void addToGroup_shouldThrowExceptionWhenUserAlreadyInGroup() {
        Group group = new Group();
        GroupUser existingGroupUser = mock(GroupUser.class);
        User existingUser = mock(User.class);

        when(existingGroupUser.getUser()).thenReturn(existingUser);
        when(existingUser.getEmail()).thenReturn("member@domain.com");

        group.setGroupUsers(List.of(existingGroupUser));

        AddToGroupDTO dto = new AddToGroupDTO("member@domain.com");

        assertThatThrownBy(() -> groupService.addToGroup(group, dto, "sender@domain.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User is already in the group");

        verifyNoInteractions(groupUserService);
    }

    @Test
    void deleteFromGroup_shouldRemoveUserWhenInGroup() {
        Group group = new Group();
        GroupUser groupUser = mock(GroupUser.class);
        GroupUser senderGroupUser = mock(GroupUser.class);
        User user = mock(User.class);
        User senderUser = mock(User.class);

        when(groupUser.getUser()).thenReturn(user);
        when(senderGroupUser.getUser()).thenReturn(senderUser);
        when(senderGroupUser.getRole()).thenReturn(GroupRole.OWNER);
        when(user.getEmail()).thenReturn("removeme@domain.com");
        when(senderUser.getEmail()).thenReturn("sender@domain.com");
        when(userService.getUserByEmail("sender@domain.com")).thenReturn(senderUser);
        group.setGroupUsers(List.of(senderGroupUser, groupUser));

        RemoveFromGroupDTO dto = new RemoveFromGroupDTO("removeme@domain.com");

        when(userService.getUserByEmail("removeme@domain.com")).thenReturn(user);
        when(groupUserService.findByGroupAndUser(group, senderUser)).thenReturn(senderGroupUser);
        when(groupUserService.findByGroupAndUser(group, user)).thenReturn(groupUser);

        groupService.deleteFromGroup(group, dto, "sender@domain.com");

        verify(groupUserService).remove(groupUser);
    }

    @Test
    void deleteFromGroup_shouldThrowExceptionWhenUserIsNotInGroup() {
        Group group = new Group();
        group.setGroupUsers(new ArrayList<>());

        RemoveFromGroupDTO dto = new RemoveFromGroupDTO("notingroup@domain.com");
        User user = mock(User.class);

        User senderUser = mock(User.class);

        when(userService.getUserByEmail("notingroup@domain.com")).thenReturn(user);
        when(userService.getUserByEmail("sender@domain.com")).thenReturn(senderUser);

        assertThatThrownBy(() -> groupService.deleteFromGroup(group, dto, "sender@domain.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User is not in the group");

        verify(groupUserService, never()).remove(any());
    }

    @Test
    void updateGroupMember_shouldReturnWhenDtosEmpty() {
        Trip trip = mock(Trip.class);
        Group group = new Group();
        groupService.updateGroupMember(trip, group, new ArrayList<>());
        verifyNoInteractions(userService, groupUserService);
    }

    @Test
    void updateGroupMember_shouldThrowWhenUserNotInGroup() {
        Trip trip = mock(Trip.class);
        Group group = new Group();
        UpdateGroupMemberDTO dto = new UpdateGroupMemberDTO("notin@domain.com", null, 0.0);
        User user = mock(User.class);
        when(userService.getUserByEmail("notin@domain.com")).thenReturn(user);
        when(groupUserService.findByGroupAndUser(group, user)).thenReturn(null);

        assertThatThrownBy(() -> groupService.updateGroupMember(trip, group, List.of(dto)))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User notin@domain.com is not in the group");
    }

    @Test
    void updateGroupMember_shouldMapDtosAndCallUpdate() {
        Trip trip = mock(Trip.class);
        Group group = new Group();
        UpdateGroupMemberDTO dto1 = new UpdateGroupMemberDTO("a@x.com", GroupRole.MEMBER, 10.0);
        UpdateGroupMemberDTO dto2 = new UpdateGroupMemberDTO("b@x.com", null, 5.0);
        User user1 = mock(User.class);
        User user2 = mock(User.class);
        when(userService.getUserByEmail("a@x.com")).thenReturn(user1);
        when(userService.getUserByEmail("b@x.com")).thenReturn(user2);
        when(groupUserService.findByGroupAndUser(group, user1)).thenReturn(mock(GroupUser.class));
        when(groupUserService.findByGroupAndUser(group, user2)).thenReturn(mock(GroupUser.class));

        groupService.updateGroupMember(trip, group, Arrays.asList(dto1, dto2));

        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(groupUserService).update(eq(trip), captor.capture());
        List<UpdateGroupUserDTO> captured = captor.getValue();
        assertThat(captured).hasSize(2);
        assertThat(captured.get(0).user()).isEqualTo(user1);
        assertThat(captured.get(0).role()).isEqualTo(GroupRole.MEMBER);
        assertThat(captured.get(0).balance()).isEqualTo(10.0);
        assertThat(captured.get(1).user()).isEqualTo(user2);
        assertThat(captured.get(1).role()).isNull();
        assertThat(captured.get(1).balance()).isEqualTo(5.0);
    }
}