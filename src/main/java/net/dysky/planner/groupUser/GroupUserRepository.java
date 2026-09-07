package net.dysky.planner.groupUser;

import net.dysky.planner.group.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

interface GroupUserRepository extends JpaRepository<GroupUser, GroupUserId> {
    List<GroupUser> findByGroup_NameAndUser_Email(String groupName, String userEmail);

    List<GroupUser> findByGroup(Group group);

    List<GroupUser> findAllByUser_EmailAndRoleNot(String userEmail, GroupRole role);

    @Query("""
    SELECT COUNT(gu) > 0
    FROM GroupUser gu
    JOIN gu.user u
    WHERE gu.group = :group
      AND u.email = :email
      AND gu.role != :excludedRole
    """)
    boolean existsByGroupAndUser_EmailAndRoleNot(
            @Param("group") Group group,
            @Param("email") String email,
            @Param("excludedRole") GroupRole excludedRole
    );
}
