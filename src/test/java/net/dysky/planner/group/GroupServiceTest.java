package net.dysky.planner.group;

import net.dysky.planner.groupUser.CreateGroupUserDTO;
import net.dysky.planner.groupUser.GroupUser;
import net.dysky.planner.groupUser.GroupUserService;
import net.dysky.planner.user.User;
import net.dysky.planner.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private GroupUserService groupUserService;

    @Mock
    private UserService userService;

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

        AddToGroupDTO dto = new AddToGroupDTO("newmember@domain.com");
        User userToAdd = mock(User.class);

        when(userService.getUserByEmail("newmember@domain.com")).thenReturn(userToAdd);

        groupService.addToGroup(group, dto);

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

        assertThatThrownBy(() -> groupService.addToGroup(group, dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User is already in the group");

        verifyNoInteractions(userService);
        verifyNoInteractions(groupUserService);
    }

    @Test
    void deleteFromGroup_shouldRemoveUserWhenInGroup() {
        Group group = new Group();
        GroupUser groupUser = mock(GroupUser.class);
        User user = mock(User.class);

        when(groupUser.getUser()).thenReturn(user);
        when(user.getEmail()).thenReturn("removeme@domain.com");
        group.setGroupUsers(List.of(groupUser));

        RemoveFromGroupDTO dto = new RemoveFromGroupDTO("Adventure Team", "removeme@domain.com");

        when(userService.getUserByEmail("removeme@domain.com")).thenReturn(user);
        when(groupUserService.findByGroupAndUser(group, user)).thenReturn(groupUser);

        groupService.deleteFromGroup(group, dto);

        verify(groupUserService).remove(groupUser);
    }

    @Test
    void deleteFromGroup_shouldThrowExceptionWhenUserIsNotInGroup() {
        Group group = new Group();
        group.setGroupUsers(new ArrayList<>());

        RemoveFromGroupDTO dto = new RemoveFromGroupDTO("Adventure Team", "notingroup@domain.com");
        User user = mock(User.class);

        when(userService.getUserByEmail("notingroup@domain.com")).thenReturn(user);

        assertThatThrownBy(() -> groupService.deleteFromGroup(group, dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User is not in the group");

        verify(groupUserService, never()).remove(any());
    }
}