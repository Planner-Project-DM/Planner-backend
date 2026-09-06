package net.dysky.planner.schedule;

import java.util.UUID;

public record ScheduleResponseDTO(
    UUID id,
    TripItemSummaryDTO tripItem,
    String startTime,
    String endTime,
    boolean allDay
) {
}
