package net.dysky.planner.tripSchedule;

import lombok.RequiredArgsConstructor;
import net.dysky.planner.schedule.CreateScheduleDTO;
import net.dysky.planner.schedule.Schedule;
import net.dysky.planner.schedule.ScheduleService;
import net.dysky.planner.trip.Trip;
import net.dysky.planner.tripItem.TripItem;
import net.dysky.planner.tripItem.TripItemService;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class TripScheduleService {

    private final TripScheduleRepository tripScheduleRepository;

    private final TripItemService tripItemService;

    private final ScheduleService scheduleService;

    public List<TripSchedule> getAllSchedulesForTrip(Trip trip) {
        return tripScheduleRepository.findAllByTripId(trip.getId());
    }

    public TripSchedule addScheduleToTrip(Trip trip, CreateTripScheduleDTO dto) {
        TripItem tripItem = tripItemService.findById(dto.tripItemId());

        Schedule schedule = scheduleService.addSchedule(new CreateScheduleDTO(tripItem, dto.startTime(), dto.endTime()));

        TripSchedule tripSchedule = new TripSchedule();

        tripSchedule.setTrip(trip);
        tripSchedule.setSchedule(schedule);
        return tripScheduleRepository.save(tripSchedule);
    }


}
