package net.dysky.planner.group;

import lombok.RequiredArgsConstructor;
import net.dysky.planner.groupUser.CreateGroupUserDTO;
import net.dysky.planner.groupUser.GroupRole;
import net.dysky.planner.groupUser.GroupUser;
import net.dysky.planner.groupUser.GroupUserService;
import net.dysky.planner.trip.TripService;
import net.dysky.planner.trip.UpdateTripDTO;
import net.dysky.planner.user.User;
import net.dysky.planner.user.UserService;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class GroupService {

    private final GroupRepository groupRepository;

    private final GroupUserService groupUserService;

    private final UserService userService;

    private final TripService tripService;

    public Group findByName(String name) {
        return groupRepository.findByName(name).orElseThrow(
                () -> new RuntimeException("Group not found"));
    }

    public Group createGroup(CreateGroupDTO createGroupDTO, String OwnerEmail) {

        Group group = new Group();
        group.setName(createGroupDTO.name());
        Group createdGroup = groupRepository.save(group);

        tripService.updateTrip(createGroupDTO.tripId(), new UpdateTripDTO(null, null, null, null, null, null, createdGroup));

        groupUserService.add(new CreateGroupUserDTO(createdGroup, userService.getUserByEmail(OwnerEmail), GroupRole.OWNER));
        return createdGroup;
    }

    public void addToGroup(AddToGroupDTO addToGroupDTO, String email) {
        Group group = findByName(addToGroupDTO.name());

        User userToAdd = userService.getUserByEmail(addToGroupDTO.email());

        if(!groupUserService.isUserInGroup(addToGroupDTO.name(), email)) {
            throw new RuntimeException("User is on the group");
        }

        groupUserService.add(new CreateGroupUserDTO(group, userToAdd, GroupRole.MEMBER));
    }

    public void deleteFromGroup(RemoveFromGroupDTO removeFromGroupDTO, String email) {
        Group group = findByName(removeFromGroupDTO.name());

        User userToRemove = userService.getUserByEmail(removeFromGroupDTO.email());

        if(!groupUserService.isUserInGroup(removeFromGroupDTO.name(), email)) {
            throw new RuntimeException("User is not on the group");
        }

        GroupUser groupUser = groupUserService.findByGroupAndUser(group, userToRemove);

        groupUserService.remove(groupUser);
    }
}
