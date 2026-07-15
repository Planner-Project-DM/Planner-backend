package net.dysky.planner.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue
    private UUID id;

    private String firstName;
    private String lastName;

    private String email;
    private String password;
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    private Boolean isActive = true;
}
