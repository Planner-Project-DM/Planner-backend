package net.dysky.planner.friendship;

import net.dysky.planner.user.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface FriendshipRepository extends JpaRepository<Friendship, UUID> {

    @EntityGraph(attributePaths = {"userSender", "userReceiver"})
    @Query("SELECT f from Friendship f WHERE (f.userSender = :user OR f.userReceiver = :user) AND f.status = 'ACCEPTED'")
    List<Friendship> findAllByUserFriends(@Param("user") User user);

    @EntityGraph(attributePaths = {"userSender", "userReceiver"})
    @Query("SELECT f from Friendship f WHERE (f.userSender = :user OR f.userReceiver = :user) AND f.status = :status")
    List<Friendship> findAllByUserFriendsAndStatus(@Param("user") User user, @Param("status") FriendshipStatus status);

    @Query("SELECT f FROM Friendship f WHERE ( " +
            "(f.userSender = :user AND f.userReceiver = :friend) OR " +
            "(f.userSender = :friend AND f.userReceiver = :user) " +
            ") AND f.status = 'ACCEPTED'")
    Optional<Friendship> findFriendshipBy(@Param("user") User user, @Param("friend") User friend);

    @Query("SELECT COUNT(f) > 0 FROM Friendship f WHERE ( " +
            "(f.userSender = :user AND f.userReceiver = :friend) OR " +
            "(f.userSender = :friend AND f.userReceiver = :user))")
    boolean existsFriendshipBy(@Param("user") User user, @Param("friend") User friend);

    @EntityGraph(attributePaths = {"userSender", "userReceiver"})
    List<Friendship> findByUserReceiver_EmailAndStatus(String userReceiverEmail, FriendshipStatus status);
}
