package net.dysky.planner.friendship;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import net.dysky.planner.user.User;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "friendships")
public class Friendship {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_sender_id")
    private User userSender;

    @ManyToOne
    @JoinColumn(name = "user_receiver_id")
    private User userReceiver;

    @Enumerated(EnumType.STRING)
    private FriendshipStatus status;

    private LocalDateTime createdAt = LocalDateTime.now();

}
