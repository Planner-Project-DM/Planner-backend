package net.dysky.planner.schedule;

import net.dysky.planner.tripItem.TripItem;

import java.time.LocalDateTime;

public record CreateScheduleDTO(
    TripItem tripItem,
    LocalDateTime startTime,
    LocalDateTime endTime
) {
}
