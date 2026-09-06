package net.dysky.planner.schedule;

import lombok.RequiredArgsConstructor;
import net.dysky.planner.trip.Trip;
import net.dysky.planner.tripSchedule.UpdateScheduleDTO;
import net.dysky.planner.tripitem.TripItemService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;

    private final TripItemService tripItemService;

    public Schedule getScheduleById(UUID scheduleId) {
        return scheduleRepository.findById(scheduleId).orElseThrow(
                () -> new IllegalArgumentException("Schedule not found"));
    }

    public Schedule addSchedule(Trip trip, CreateScheduleDTO createScheduleDTO) {
        Schedule schedule = new Schedule();

        schedule.setTripItem(createScheduleDTO.tripItem());
        schedule.setAllDay(createScheduleDTO.allDay());

        if (createScheduleDTO.allDay()) {
            schedule.setStartTime(createScheduleDTO.startTime());
            schedule.setEndTime(null);
            return scheduleRepository.save(schedule);
        }

        isDateValid(trip.getId(), createScheduleDTO.startTime(), createScheduleDTO.endTime(), trip.getStartDate().atStartOfDay(), trip.getEndDate().atTime(23, 59));
        schedule.setStartTime(createScheduleDTO.startTime());
        schedule.setEndTime(createScheduleDTO.endTime());

        return scheduleRepository.save(schedule);
    }

    public Schedule updateSchedule(Trip trip, UpdateScheduleDTO dto) {
        Schedule schedule = getScheduleById(dto.scheduleId());

        if(dto.allDay() != null) {
            schedule.setAllDay(dto.allDay());
        }

        if(schedule.isAllDay()) {
            schedule.setStartTime(dto.startTime());
            schedule.setEndTime(null);
            return scheduleRepository.save(schedule);
        }

        if(dto.tripItem() != null) {
            schedule.setTripItem(tripItemService.findById(dto.tripItem()));
        }

        if(dto.startTime() != null) {
            schedule.setStartTime(dto.startTime());
        }

        if(dto.endTime() != null) {
            schedule.setEndTime(dto.endTime());
        }

        if(schedule.getStartTime() != null && schedule.getEndTime() != null) {
            isDateValid(trip.getId(), schedule.getStartTime(), schedule.getEndTime(), trip.getStartDate().atStartOfDay(), trip.getEndDate().atTime(23, 59), dto.scheduleId());
        }

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
