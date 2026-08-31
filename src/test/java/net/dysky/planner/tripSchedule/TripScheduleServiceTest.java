package net.dysky.planner.tripSchedule;

import net.dysky.planner.schedule.*;
import net.dysky.planner.trip.Trip;
import net.dysky.planner.tripitem.TripItem;
import net.dysky.planner.tripitem.TripItemService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripScheduleServiceTest {

    @Mock
    private TripScheduleRepository tripScheduleRepository;

    @Mock
    private TripItemService tripItemService;

    @Mock
    private ScheduleService scheduleService;

    @InjectMocks
    private TripScheduleService tripScheduleService;

    @Test
    void findByTripAndSchedule_shouldReturnTripSchedule_whenExists() {
        UUID tripId = UUID.randomUUID();
        UUID scheduleId = UUID.randomUUID();
        TripSchedule expected = new TripSchedule();

        when(tripScheduleRepository.findByTrip_IdAndSchedule_Id(tripId, scheduleId))
                .thenReturn(Optional.of(expected));

        TripSchedule actual = tripScheduleService.findByTripAndSchedule(tripId, scheduleId);

        assertNotNull(actual);
        assertEquals(expected, actual);
        verify(tripScheduleRepository, times(1)).findByTrip_IdAndSchedule_Id(tripId, scheduleId);
    }

    @Test
    void findByTripAndSchedule_shouldThrowException_whenNotFound() {
        UUID tripId = UUID.randomUUID();
        UUID scheduleId = UUID.randomUUID();

        when(tripScheduleRepository.findByTrip_IdAndSchedule_Id(tripId, scheduleId))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> tripScheduleService.findByTripAndSchedule(tripId, scheduleId));

        assertEquals("Schedule not found for the given trip", exception.getMessage());
        verify(tripScheduleRepository, times(1)).findByTrip_IdAndSchedule_Id(tripId, scheduleId);
    }

    @Test
    void getAllSchedulesForTrip_shouldReturnListOfSchedules() {
        UUID tripId = UUID.randomUUID();
        Trip trip = new Trip();
        trip.setId(tripId);

        List<TripSchedule> expected = List.of(new TripSchedule());
        when(tripScheduleRepository.findAllByTripId(tripId)).thenReturn(expected);

        List<TripSchedule> actual = tripScheduleService.getAllSchedulesForTrip(trip);

        assertNotNull(actual);
        assertEquals(expected.size(), actual.size());
        verify(tripScheduleRepository, times(1)).findAllByTripId(tripId);
    }

    @Test
    void addScheduleToTrip_shouldSaveAndReturnTripSchedule() {
        // Given
        Trip trip = new Trip();
        UUID tripItemId = UUID.randomUUID();
        LocalDateTime startTime = LocalDateTime.of(2026, 8, 2, 10, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 8, 2, 12, 0);
        CreateTripScheduleDTO dto = new CreateTripScheduleDTO(tripItemId, startTime, endTime);

        TripItem tripItem = new TripItem();
        Schedule schedule = new Schedule();

        when(tripItemService.findById(tripItemId)).thenReturn(tripItem);
        when(scheduleService.addSchedule(eq(trip), any(CreateScheduleDTO.class))).thenReturn(schedule);
        when(tripScheduleRepository.save(any(TripSchedule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        TripSchedule result = tripScheduleService.addScheduleToTrip(trip, dto);

        // Then
        assertNotNull(result);
        assertEquals(trip, result.getTrip());
        assertEquals(schedule, result.getSchedule());

        verify(tripItemService, times(1)).findById(tripItemId);
        verify(scheduleService, times(1)).addSchedule(eq(trip), any(CreateScheduleDTO.class));
        verify(tripScheduleRepository, times(1)).save(any(TripSchedule.class));
    }

    @Test
    void deleteScheduleFromTrip_shouldDeleteSuccessfully_whenExists() {
        // Given
        UUID tripId = UUID.randomUUID();
        UUID scheduleId = UUID.randomUUID();
        TripSchedule tripSchedule = new TripSchedule();

        when(tripScheduleRepository.findByTrip_IdAndSchedule_Id(tripId, scheduleId))
                .thenReturn(Optional.of(tripSchedule));

        // When
        tripScheduleService.deleteScheduleFromTrip(tripId, scheduleId);

        // Then
        verify(tripScheduleRepository, times(1)).findByTrip_IdAndSchedule_Id(tripId, scheduleId);
        verify(tripScheduleRepository, times(1)).delete(tripSchedule);
        verify(scheduleService, times(1)).deleteSchedule(scheduleId);
    }

    @Test
    void mapToDTO_shouldCorrectlyMapProperties() {
        // Given
        UUID scheduleId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        LocalDateTime startTime = LocalDateTime.of(2026, 8, 2, 10, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 8, 2, 12, 0);

        TripItem tripItem = mock(TripItem.class);
        when(tripItem.getId()).thenReturn(itemId);
        when(tripItem.getName()).thenReturn("Hotel Marriott");

        Schedule schedule = mock(Schedule.class);
        when(schedule.getTripItem()).thenReturn(tripItem);
        when(schedule.getId()).thenReturn(scheduleId);
        when(schedule.getStartTime()).thenReturn(startTime);
        when(schedule.getEndTime()).thenReturn(endTime);

        TripSchedule tripSchedule = new TripSchedule();
        tripSchedule.setSchedule(schedule);

        // When
        ScheduleResponseDTO result = tripScheduleService.mapToDTO(tripSchedule);

        // Then
        assertNotNull(result);
        assertEquals(scheduleId, result.id());
        assertEquals(itemId, result.tripItem().id());
        assertEquals("Hotel Marriott", result.tripItem().name());
        assertEquals("2026-08-02T10:00", result.startTime());
        assertEquals("2026-08-02T12:00", result.endTime());
    }

    @Test
    void mapToDTO_shouldReturnNullDates_whenScheduleIsAllDay() {
        UUID scheduleId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        TripItem tripItem = mock(TripItem.class);
        when(tripItem.getId()).thenReturn(itemId);
        when(tripItem.getName()).thenReturn("Cały dzień");

        Schedule schedule = mock(Schedule.class);
        when(schedule.getTripItem()).thenReturn(tripItem);
        when(schedule.getId()).thenReturn(scheduleId);
        when(schedule.getStartTime()).thenReturn(null);
        when(schedule.getEndTime()).thenReturn(null);

        TripSchedule tripSchedule = new TripSchedule();
        tripSchedule.setSchedule(schedule);

        ScheduleResponseDTO result = tripScheduleService.mapToDTO(tripSchedule);

        assertNotNull(result);
        assertNull(result.startTime());
        assertNull(result.endTime());
        assertEquals("Cały dzień", result.tripItem().name());
    }
}