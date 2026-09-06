package net.dysky.planner.usersetings;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import net.dysky.planner.user.User;

import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "user_settings")
public class UserSettings {

    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    private Currency currency;

    private Double budgetLimit;

    @Enumerated(EnumType.STRING)
    private Language language;

}