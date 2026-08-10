package net.dysky.planner.groupUser;

import net.dysky.planner.group.Group;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface GroupUserRepository extends JpaRepository<GroupUser, GroupUserId> {
    List<GroupUser> findByGroup_NameAndUser_Email(String groupName, String userEmail);

    List<GroupUser> findByGroup(Group group);
}
