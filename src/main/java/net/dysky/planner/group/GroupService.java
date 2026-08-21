package net.dysky.planner.group;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.dysky.planner.exception.UserInGroupException;
import net.dysky.planner.exception.UserNotFoundException;
import net.dysky.planner.groupUser.*;
import net.dysky.planner.trip.Trip;
import net.dysky.planner.user.User;
import net.dysky.planner.user.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
@Transactional
public class GroupService {

    private final GroupRepository groupRepository;

    private final GroupUserService groupUserService;

    private final UserService userService;

    public Group findByName(String name) {
        return groupRepository.findByName(name).orElseThrow(
                () -> new RuntimeException("Group not found"));
    }

    public Group createGroup(CreateGroupDTO createGroupDTO, String OwnerEmail) {
        Group group = new Group();
        group.setName(createGroupDTO.name());
        Group createdGroup = groupRepository.save(group);

        groupUserService.add(new CreateGroupUserDTO(createdGroup, userService.getUserByEmail(OwnerEmail), GroupRole.OWNER));
        return createdGroup;
    }

    public void addToGroup(Group group, AddToGroupDTO addToGroupDTO) {
        boolean alreadyInGroup = group.getGroupUsers().stream()
                .anyMatch(gu -> gu.getUser().getEmail().equals(addToGroupDTO.email()));
        if (alreadyInGroup) throw new UserInGroupException("User is already in the group");

        User userToAdd = userService.getUserByEmail(addToGroupDTO.email());
        groupUserService.add(new CreateGroupUserDTO(group, userToAdd, GroupRole.MEMBER));
    }

    public void updateGroupMember(Trip trip, Group group, List<UpdateGroupMemberDTO> dtos) {
        if(dtos.isEmpty()) return;

        dtos.stream().filter(dto -> groupUserService.findByGroupAndUser(group, userService.getUserByEmail(dto.email())) == null)
                .findFirst()
                .ifPresent(dto -> {
                    throw new UserNotFoundException("User " + dto.email() + " is not in the group");
                });

        List<UpdateGroupUserDTO> list = dtos.stream()
                        .map(dto -> new UpdateGroupUserDTO(group, userService.getUserByEmail(dto.email()), dto.role() != null ? dto.role() : null, dto.balance()))
                        .toList();

        groupUserService.update(trip, list);
    }

    public void deleteFromGroup(Group group, RemoveFromGroupDTO removeFromGroupDTO) {
        User userToRemove = userService.getUserByEmail(removeFromGroupDTO.email());

        boolean inGroup = group.getGroupUsers().stream()
                .anyMatch(gu -> gu.getUser().getEmail().equals(removeFromGroupDTO.email()));
        if (!inGroup) throw new UserNotFoundException("User is not in the group");

        GroupUser groupUser = groupUserService.findByGroupAndUser(group, userToRemove);

        groupUserService.remove(groupUser);
    }
}
