package net.dysky.planner.groupUser;

import lombok.RequiredArgsConstructor;
import net.dysky.planner.group.Group;
import net.dysky.planner.user.User;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class GroupUserService {

    private final GroupUserRepository groupUserRepository;

    public GroupUser findByGroupAndUser(Group group, User user) {
        return groupUserRepository.findById(new GroupUserId(group.getId(), user.getId()))
                .orElseThrow(() -> new RuntimeException("GroupUser not found"));
    }

    public GroupUser add(CreateGroupUserDTO createGroupUserDTO) {
        GroupUser groupUser = new GroupUser();

        groupUser.setGroup(createGroupUserDTO.group());
        groupUser.setUser(createGroupUserDTO.user());
        groupUser.setRole(createGroupUserDTO.role());

        return groupUserRepository.save(groupUser);
    }

    public boolean isUserInGroup(String groupName, String userEmail) {
        return !groupUserRepository.findByGroup_NameAndUser_Email(groupName, userEmail).isEmpty();
    }

    public void remove(GroupUser groupUser) {
        groupUserRepository.delete(groupUser);
    }


}
