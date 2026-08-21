package net.dysky.planner.groupUser;

import net.dysky.planner.group.Group;
import net.dysky.planner.user.User;

public record UpdateGroupUserDTO(
        Group group,
        User user,
        GroupRole role,
        Double balance
) {
}
