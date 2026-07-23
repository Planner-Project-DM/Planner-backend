package net.dysky.planner.groupUser;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class GroupUserId implements Serializable {

    UUID group;
    UUID user;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        GroupUserId that = (GroupUserId) obj;
        return group.equals(that.group) && user.equals(that.user);
    }

    @Override
    public int hashCode() {
        return group.hashCode() + user.hashCode();
    }
}
