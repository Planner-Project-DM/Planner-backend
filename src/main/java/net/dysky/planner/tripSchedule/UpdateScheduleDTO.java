package net.dysky.planner.tripSchedule;

import java.time.LocalDateTime;
import java.util.UUID;

public record UpdateScheduleDTO(
    UUID scheduleId,
    UUID tripItem,
    LocalDateTime startTime,
    LocalDateTime endTime,
    Boolean allDay
) {
    public UpdateScheduleDTO(UUID scheduleId, UUID tripItem, LocalDateTime startTime, LocalDateTime endTime) {
        this(scheduleId, tripItem, startTime, endTime, null);
    }
}
