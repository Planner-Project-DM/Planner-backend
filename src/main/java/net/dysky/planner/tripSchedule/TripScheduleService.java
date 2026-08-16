package net.dysky.planner.tripSchedule;

import lombok.RequiredArgsConstructor;
import net.dysky.planner.schedule.*;
import net.dysky.planner.trip.Trip;
import net.dysky.planner.tripItem.TripItem;
import net.dysky.planner.tripItem.TripItemService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class TripScheduleService {

    private final TripScheduleRepository tripScheduleRepository;

    private final TripItemService tripItemService;

    private final ScheduleService scheduleService;

    public TripSchedule findByTripAndSchedule(UUID tripId, UUID scheduleId) {
        return tripScheduleRepository.findByTrip_IdAndSchedule_Id(tripId, scheduleId).orElseThrow(
                () -> new IllegalArgumentException("Schedule not found for the given trip"));
    }

    public List<TripSchedule> getAllSchedulesForTrip(Trip trip) {
        return tripScheduleRepository.findAllByTripId(trip.getId());
    }

    public TripSchedule addScheduleToTrip(Trip trip, CreateTripScheduleDTO dto) {
        TripItem tripItem = tripItemService.findById(dto.tripItemId());

        Schedule schedule = scheduleService.addSchedule(trip, new CreateScheduleDTO(tripItem, dto.startTime(), dto.endTime()));

        TripSchedule tripSchedule = new TripSchedule();

        tripSchedule.setTrip(trip);
        tripSchedule.setSchedule(schedule);
        return tripScheduleRepository.save(tripSchedule);
    }

    public void deleteScheduleFromTrip(UUID tripId, UUID scheduleId) {
        TripSchedule tripSchedule = findByTripAndSchedule(tripId, scheduleId);

        tripScheduleRepository.delete(tripSchedule);

        scheduleService.deleteSchedule(scheduleId);
    }

    public ScheduleResponseDTO mapToDTO(TripSchedule tripSchedule) {
        TripItemSummaryDTO tripItemSummaryDTO = new TripItemSummaryDTO(
                tripSchedule.schedule.getTripItem().getId(),
                tripSchedule.schedule.getTripItem().getName()
        );

        return new ScheduleResponseDTO(
                tripSchedule.schedule.getId(),
                tripItemSummaryDTO,
                tripSchedule.schedule.getStartTime().toString(),
                tripSchedule.schedule.getEndTime().toString()
        );
    }

}
