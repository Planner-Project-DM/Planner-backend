package net.dysky.planner.friendship;

import net.dysky.planner.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface FriendshipRepository extends JpaRepository<Friendship, UUID> {
    @Query("SELECT f from Friendship f WHERE (f.userSender = :user OR f.userReceiver = :user) AND f.status = 'ACCEPTED'")
    List<Friendship> findAllByUserFriends(@Param("user") User user);

    @Query("SELECT f FROM Friendship f WHERE ( " +
            "(f.userSender = :user AND f.userReceiver = :friend) OR " +
            "(f.userSender = :friend AND f.userReceiver = :user) " +
            ") AND f.status = 'ACCEPTED'")
    Optional<Friendship> findFriendshipBy(@Param("user") User user, @Param("friend") User friend);

}
