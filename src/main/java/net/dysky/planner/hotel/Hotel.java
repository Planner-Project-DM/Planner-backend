package net.dysky.planner.hotel;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;
import net.dysky.planner.address.Address;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "hotels")
public class Hotel {

    @Id
    @GeneratedValue
    private UUID id;

    private String name;

    @Embedded
    private Address address;

    private String phoneNumber;

    @Email
    private String email;

    private String website;

    private String description;

    @Embedded
    private Location location;
}
