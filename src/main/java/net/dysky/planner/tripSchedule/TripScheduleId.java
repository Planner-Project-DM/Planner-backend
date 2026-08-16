package net.dysky.planner.tripSchedule;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TripScheduleId implements Serializable {

    UUID trip;
    UUID schedule;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TripScheduleId that = (TripScheduleId) obj;
        return trip.equals(that.trip) && schedule.equals(that.schedule);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(trip, schedule);
    }
}
