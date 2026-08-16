package net.dysky.planner.tripSchedule;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateTripScheduleDTO(
        UUID tripItemId,
        LocalDateTime startTime,
        LocalDateTime endTime
) {
}
