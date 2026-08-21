package net.dysky.planner.groupUser;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import net.dysky.planner.group.Group;
import net.dysky.planner.user.User;

@Getter
@Setter
@Entity
@IdClass(GroupUserId.class)
@Table(name = "group_user")
public class GroupUser {

    @Id
    @ManyToOne
    @JoinColumn(name = "group_id")
    @JsonIgnoreProperties("groupUsers")
    private Group group;

    @Id
    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties("groupUsers")
    private User user;

    @Enumerated(EnumType.STRING)
    private GroupRole role = GroupRole.MEMBER;

    private Double balance = 0.0;
}
