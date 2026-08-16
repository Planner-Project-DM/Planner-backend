package net.dysky.planner.tripItem;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.dysky.planner.address.Address;
import net.dysky.planner.hotel.Location;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "trip_items")
public class TripItem {
    @Id
    @GeneratedValue
    private UUID id;

    private String name;

    @Enumerated(EnumType.STRING)
    private TripItemCategory category;

    @Embedded
    private Address address;

    private String phoneNumber;

    private String email;

    private String website;

    private String description;

    @Embedded
    private Location location;

    private String stars;

    private Double price;

    private String tourism;
}
