package net.dysky.planner.schedule;

import net.dysky.planner.tripitem.TripItem;

import java.time.LocalDateTime;

public record CreateScheduleDTO(
    TripItem tripItem,
    LocalDateTime startTime,
    LocalDateTime endTime,
    boolean allDay
) {
    public CreateScheduleDTO(TripItem tripItem, LocalDateTime startTime, LocalDateTime endTime) {
        this(tripItem, startTime, endTime, false);
    }
}
