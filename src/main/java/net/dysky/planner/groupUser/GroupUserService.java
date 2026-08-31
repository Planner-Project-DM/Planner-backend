package net.dysky.planner.groupUser;

import lombok.RequiredArgsConstructor;
import net.dysky.planner.exception.BalanceOverBudgetException;
import net.dysky.planner.exception.HasNoPermissionException;
import net.dysky.planner.group.Group;
import net.dysky.planner.trip.Trip;
import net.dysky.planner.user.User;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class GroupUserService {

    private final GroupUserRepository groupUserRepository;

    public GroupUser findByGroupAndUser(Group group, User user) {
        return groupUserRepository.findById(new GroupUserId(group.getId(), user.getId()))
                .orElseThrow(() -> new RuntimeException("GroupUser not found"));
    }

    public List<GroupUser> findALlByUserEmail(String userEmail, GroupRole role) {
        return groupUserRepository.findAllByUser_EmailAndRoleNot(userEmail, role);
    }

    public boolean isUserOwnerOrAdminOfGroup(Group group, String email) {
        return groupUserRepository.existsByGroupAndUser_EmailAndRoleNot(group, email, GroupRole.MEMBER);
    }

    public GroupUser add(CreateGroupUserDTO createGroupUserDTO) {
        GroupUser groupUser = new GroupUser();

        groupUser.setGroup(createGroupUserDTO.group());
        groupUser.setUser(createGroupUserDTO.user());
        groupUser.setRole(createGroupUserDTO.role());

        return groupUserRepository.save(groupUser);
    }

    public void update(Trip trip, List<UpdateGroupUserDTO> updateGroupUserDTOList) {

        double totalBalanceChange = updateGroupUserDTOList.stream().mapToDouble(dto -> dto.balance() != null ? dto.balance() : 0).sum();

        if(totalBalanceChange > trip.getBudget()) {
            throw new BalanceOverBudgetException("Total balance of group users exceeds total cost of trip");
        }

        for(UpdateGroupUserDTO dto : updateGroupUserDTOList) {
            GroupUser groupUser = findByGroupAndUser(dto.group(), dto.user());

            if (dto.role() != null) {
                groupUser.setRole(dto.role());
            }

            if(dto.balance() != null) {
                groupUser.setBalance(dto.balance());
            }

            groupUserRepository.save(groupUser);
        }
    }

    public boolean isUserInGroup(String groupName, String userEmail) {
        return !groupUserRepository.findByGroup_NameAndUser_Email(groupName, userEmail).isEmpty();
    }

    public void remove(GroupUser groupUser) {
        if(!groupUser.getRole().equals(GroupRole.OWNER)) {
            throw new HasNoPermissionException("Only the owner of the group can remove a user from the group");
        }

        groupUserRepository.delete(groupUser);
    }

}
