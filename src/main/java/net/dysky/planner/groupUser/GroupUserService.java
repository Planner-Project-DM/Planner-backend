package net.dysky.planner.groupUser;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class GroupUserService {

    private final GroupUserRepository groupUserRepository;

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
}
