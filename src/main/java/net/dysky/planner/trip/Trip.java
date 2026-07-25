package net.dysky.planner.trip;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import net.dysky.planner.group.Group;
import net.dysky.planner.user.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "trips", uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_trip_name_and_creator",
                columnNames = {"name", "trip_creator_id"}
        )
})
public class Trip {

    @Id
    @GeneratedValue
    private UUID id;

    private String name;
    private String destination;

    @Enumerated(EnumType.STRING)
    private TripStatus status;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "trip_creator_id", referencedColumnName = "id")
    private User tripCreator;

    private Double budget;

    @OneToOne
    private Group tripGroup;

    private LocalDate startDate;
    private LocalDate endDate;

    private LocalDateTime createdAt = LocalDateTime.now();

}
