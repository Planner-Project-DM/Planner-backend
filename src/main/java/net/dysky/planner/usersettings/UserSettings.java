package net.dysky.planner.usersettings;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import net.dysky.planner.notification.NotificationChannel;
import net.dysky.planner.user.User;

import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "user_settings")
public class UserSettings {

    @Id
    @JsonIgnore
    @GeneratedValue
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    private Currency currency = Currency.PLN;

    private Double budgetLimit;

    @Enumerated(EnumType.STRING)
    private Language language = Language.PL;

    @Column(nullable = false)
    private boolean isNotificationEnabled = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationChannel notificationChannel = NotificationChannel.PUSH;

}