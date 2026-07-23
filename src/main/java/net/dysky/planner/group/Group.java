package net.dysky.planner.group;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import net.dysky.planner.groupUser.GroupUser;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "groups")
public class Group {

    @Id
    @GeneratedValue
    private UUID id;

    private String name;

    @OneToMany(mappedBy = "group")
    private List<GroupUser> groupUsers;
}
