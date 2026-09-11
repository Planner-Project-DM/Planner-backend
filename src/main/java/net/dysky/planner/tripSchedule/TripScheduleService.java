package net.dysky.planner.tripSchedule;

import lombok.RequiredArgsConstructor;
import net.dysky.planner.notification.NotificationService;
import net.dysky.planner.schedule.*;
import net.dysky.planner.trip.Trip;
import net.dysky.planner.tripitem.TripItem;
import net.dysky.planner.tripitem.TripItemService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class TripScheduleService {

    private final TripScheduleRepository tripScheduleRepository;

    private final TripItemService tripItemService;

    private final ScheduleService scheduleService;

    private final NotificationService notificationService;

    public TripSchedule findByTripAndSchedule(UUID tripId, UUID scheduleId) {
        return tripScheduleRepository.findByTrip_IdAndSchedule_Id(tripId, scheduleId).orElseThrow(
                () -> new IllegalArgumentException("Schedule not found for the given trip"));
    }

    public List<TripSchedule> getAllSchedulesForTrip(Trip trip) {
        return tripScheduleRepository.findAllByTripId(trip.getId());
    }

    public TripSchedule addScheduleToTrip(Trip trip, CreateTripScheduleDTO dto, String email) {
        TripItem tripItem = tripItemService.findById(dto.tripItemId());

        Schedule schedule = scheduleService.addSchedule(trip, new CreateScheduleDTO(tripItem, dto.startTime(), dto.endTime(), dto.allDay()));

        TripSchedule tripSchedule = new TripSchedule();

        tripSchedule.setTrip(trip);
        tripSchedule.setSchedule(schedule);

        trip.getTripGroup().getGroupUsers().forEach(groupUser ->
            notificationService.createNotification("New schedule added to trip", "Schedule for " + tripItem.getName() + " has been added to your trip. Created by: " + email, groupUser.getUser().getId())
        );

        return tripScheduleRepository.save(tripSchedule);
    }

    public TripSchedule updateScheduleInTrip(Trip trip, UpdateScheduleDTO dto, String email) {
        Schedule schedule = scheduleService.updateSchedule(trip, dto);

        trip.getTripGroup().getGroupUsers().forEach(groupUser ->
                notificationService.createNotification(
                        "Schedule updated in trip",
                        "Schedule for " + schedule.getTripItem().getName() + " has been updated in your trip. Updated by: " + email,
                        groupUser.getUser().getId()
                )
        );

        return findByTripAndSchedule(trip.getId(), schedule.getId());
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
                tripSchedule.schedule.getStartTime() == null ? null : tripSchedule.schedule.getStartTime().toString(),
                tripSchedule.schedule.getEndTime() == null ? null : tripSchedule.schedule.getEndTime().toString(),
                tripSchedule.schedule.isAllDay()
        );
    }

}
