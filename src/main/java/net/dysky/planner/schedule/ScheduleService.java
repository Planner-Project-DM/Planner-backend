package net.dysky.planner.schedule;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;

    public Schedule addSchedule(CreateScheduleDTO createScheduleDTO) {
        Schedule schedule = new Schedule();

        schedule.setTripItem(createScheduleDTO.tripItem());

        if(createScheduleDTO.startTime().isAfter(createScheduleDTO.endTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        if(createScheduleDTO.startTime().isEqual(createScheduleDTO.endTime())) {
            throw new IllegalArgumentException("Start time and end time cannot be the same");
        }

        if(scheduleRepository.existsOverlapping(createScheduleDTO.startTime(), createScheduleDTO.endTime())) {
            throw new IllegalArgumentException("Schedule overlaps with an existing schedule");
        }

        schedule.setStartTime(createScheduleDTO.startTime());
        schedule.setEndTime(createScheduleDTO.endTime());

        return scheduleRepository.save(schedule);
    }
}
