package net.dysky.planner.tripSchedule;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import net.dysky.planner.schedule.Schedule;
import net.dysky.planner.trip.Trip;

@Getter
@Setter
@Entity
@IdClass(TripScheduleId.class)
@Table(name = "trip_schedules")
public class TripSchedule {

    @Id
    @JoinColumn(name = "trip_id")
    @ManyToOne
    Trip trip;

    @Id
    @JoinColumn(name = "schedule_id")
    @ManyToOne
    Schedule schedule;

}
