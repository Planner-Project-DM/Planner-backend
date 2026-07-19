package net.dysky.planner.trip;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import net.dysky.planner.user.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "trips")
public class Trip {

    @Id
    @GeneratedValue
    private UUID id;

    private String name;
    private String destination;

    @Enumerated(EnumType.STRING)
    private TripStatus status;

    @OneToOne
    @JoinColumn(name = "trip_creator_id", referencedColumnName = "id")
    private User tripCreator;

    private Double budget;

    private List<User> group;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private LocalDateTime createdAt;

}
