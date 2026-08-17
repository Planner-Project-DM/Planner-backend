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

        isDateValid(createScheduleDTO.startTime(), createScheduleDTO.endTime(), trip.getStartDate().atStartOfDay(), trip.getEndDate().atTime(23, 59));

        schedule.setStartTime(createScheduleDTO.startTime());
        schedule.setEndTime(createScheduleDTO.endTime());

        return scheduleRepository.save(schedule);
    }

    public Schedule updateSchedule(Trip trip, UpdateScheduleDTO dto) {
        Schedule schedule = getScheduleById(dto.scheduleId());

        isDateValid(dto.startTime(), dto.endTime(), trip.getStartDate().atStartOfDay(), trip.getEndDate().atTime(23, 59));

        if(dto.tripItem() == null) {
            throw new IllegalArgumentException("Trip item cannot be null");
        } else if(schedule.getTripItem().getId().equals(dto.tripItem())) {
            throw new IllegalArgumentException("Trip item is the same as the current one");
        } else {
            schedule.setTripItem(schedule.getTripItem());
        }

        schedule.setStartTime(dto.startTime());
        schedule.setEndTime(dto.endTime());

        return scheduleRepository.save(schedule);
    }

    public void isDateValid(LocalDateTime startTime, LocalDateTime endTime, LocalDateTime tripStartDate, LocalDateTime tripEndDate) {
        if(startTime.isBefore(tripStartDate) && endTime.isBefore(tripEndDate)) {
            throw new IllegalArgumentException("Schedule must be within the trip dates");
        }

        if(startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        if(startTime.isEqual(endTime)) {
            throw new IllegalArgumentException("Start time and end time cannot be the same");
        }

        if(scheduleRepository.existsOverlapping(startTime, endTime)) {
            throw new IllegalArgumentException("Schedule overlaps with an existing schedule");
        }
    }

    public void deleteSchedule(UUID scheduleId) {
        Schedule schedule = getScheduleById(scheduleId);

        scheduleRepository.delete(schedule);
    }

}
