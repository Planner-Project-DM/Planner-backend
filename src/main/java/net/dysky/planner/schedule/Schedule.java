package net.dysky.planner.schedule;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.dysky.planner.tripItem.TripItem;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "schedules")
@NoArgsConstructor
@AllArgsConstructor
public class Schedule {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "trip_item_id", referencedColumnName = "id")
    private TripItem tripItem;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private LocalDateTime createdAt = LocalDateTime.now();
}
