package net.dysky.planner.schedule;

import lombok.RequiredArgsConstructor;
import net.dysky.planner.trip.Trip;
import net.dysky.planner.tripSchedule.UpdateScheduleDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;

    public Schedule getScheduleById(UUID scheduleId) {
        return scheduleRepository.findById(scheduleId).orElseThrow(
                () -> new IllegalArgumentException("Schedule not found"));
    }

    public Schedule addSchedule(Trip trip, CreateScheduleDTO createScheduleDTO) {
        Schedule schedule = new Schedule();

        schedule.setTripItem(createScheduleDTO.tripItem());

        isDateValid(trip.getId(), createScheduleDTO.startTime(), createScheduleDTO.endTime(), trip.getStartDate().atStartOfDay(), trip.getEndDate().atTime(23, 59));

        schedule.setStartTime(createScheduleDTO.startTime());
        schedule.setEndTime(createScheduleDTO.endTime());

        return scheduleRepository.save(schedule);
    }

    public Schedule updateSchedule(Trip trip, UpdateScheduleDTO dto) {
        Schedule schedule = getScheduleById(dto.scheduleId());

        isDateValid(trip.getId(), dto.startTime(), dto.endTime(), trip.getStartDate().atStartOfDay(), trip.getEndDate().atTime(23, 59), dto.scheduleId());

        if(dto.tripItem() != null) {
            schedule.setTripItem(schedule.getTripItem());
        }

        schedule.setStartTime(dto.startTime());
        schedule.setEndTime(dto.endTime());

        return scheduleRepository.save(schedule);
    }

    public void isDateValid(LocalDateTime startTime, LocalDateTime endTime, LocalDateTime tripStartDate, LocalDateTime tripEndDate) {
        isDateValid(null, startTime, endTime, tripStartDate, tripEndDate, null);
    }

    public void isDateValid(UUID tripId, LocalDateTime startTime, LocalDateTime endTime, LocalDateTime tripStartDate, LocalDateTime tripEndDate) {
        isDateValid(tripId, startTime, endTime, tripStartDate, tripEndDate, null);
    }

    public void isDateValid(LocalDateTime startTime, LocalDateTime endTime, LocalDateTime tripStartDate, LocalDateTime tripEndDate, UUID scheduleId) {
        isDateValid(null, startTime, endTime, tripStartDate, tripEndDate, scheduleId);
    }

    public void isDateValid(UUID tripId, LocalDateTime startTime, LocalDateTime endTime, LocalDateTime tripStartDate, LocalDateTime tripEndDate, UUID scheduleId) {
        checkDate(startTime, endTime, tripStartDate, tripEndDate);

        boolean overlaps = tripId == null
                ? scheduleRepository.existsOverlapping(startTime, endTime)
                : scheduleRepository.existsOverlapping(tripId, startTime, endTime, scheduleId);

        if(overlaps) {
            throw new IllegalArgumentException("Schedule overlaps with an existing schedule");
        }
    }

    public void checkDate(LocalDateTime startTime, LocalDateTime endTime, LocalDateTime tripStartDate, LocalDateTime tripEndDate) {
        if(startTime.isBefore(tripStartDate) && endTime.isBefore(tripEndDate)) {
            throw new IllegalArgumentException("Schedule must be within the trip dates");
        }

        if(startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        if(startTime.isEqual(endTime)) {
            throw new IllegalArgumentException("Start time and end time cannot be the same");
        }
    }

    public void deleteSchedule(UUID scheduleId) {
        Schedule schedule = getScheduleById(scheduleId);

        scheduleRepository.delete(schedule);
    }

}
