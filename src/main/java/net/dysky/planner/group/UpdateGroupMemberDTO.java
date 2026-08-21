package net.dysky.planner.group;

import net.dysky.planner.groupUser.GroupRole;

public record UpdateGroupMemberDTO(
    String email,
    GroupRole role,
    Double balance
) {
}
