package net.dysky.planner.schedule;

import net.dysky.planner.trip.Trip;
import net.dysky.planner.tripitem.TripItem;
import net.dysky.planner.tripSchedule.UpdateScheduleDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock
    private ScheduleRepository scheduleRepository;

    @InjectMocks
    private ScheduleService scheduleService;

    @Test
    void getScheduleById_shouldReturnSchedule_whenExists() {
        UUID id = UUID.randomUUID();
        Schedule expected = new Schedule();
        expected.setId(id);

        when(scheduleRepository.findById(id)).thenReturn(Optional.of(expected));

        Schedule actual = scheduleService.getScheduleById(id);

        assertNotNull(actual);
        assertEquals(expected, actual);
        verify(scheduleRepository, times(1)).findById(id);
    }

    @Test
    void getScheduleById_shouldThrowException_whenNotFound() {
        UUID id = UUID.randomUUID();
        when(scheduleRepository.findById(id)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> scheduleService.getScheduleById(id));

        assertEquals("Schedule not found", exception.getMessage());
        verify(scheduleRepository, times(1)).findById(id);
    }

    @Test
    void addSchedule_shouldSaveAndReturnSchedule_whenDataIsValid() {
        // Given
        Trip trip = new Trip();
        trip.setStartDate(LocalDate.of(2026, 8, 1));
        trip.setEndDate(LocalDate.of(2026, 8, 10));

        TripItem tripItem = new TripItem();
        LocalDateTime startTime = LocalDateTime.of(2026, 8, 2, 10, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 8, 2, 12, 0);
        CreateScheduleDTO dto = new CreateScheduleDTO(tripItem, startTime, endTime);

        when(scheduleRepository.existsOverlapping(startTime, endTime)).thenReturn(false);
        when(scheduleRepository.save(any(Schedule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Schedule result = scheduleService.addSchedule(trip, dto);

        // Then
        assertNotNull(result);
        assertEquals(tripItem, result.getTripItem());
        assertEquals(startTime, result.getStartTime());
        assertEquals(endTime, result.getEndTime());

        verify(scheduleRepository, times(1)).existsOverlapping(startTime, endTime);
        verify(scheduleRepository, times(1)).save(any(Schedule.class));
    }

    @Test
    void addSchedule_shouldThrowException_whenDatesAreOutsideTrip() {
        // Given
        Trip trip = new Trip();
        trip.setStartDate(LocalDate.of(2026, 8, 1));
        trip.setEndDate(LocalDate.of(2026, 8, 10));

        TripItem tripItem = new TripItem();
        LocalDateTime startTime = LocalDateTime.of(2026, 7, 31, 10, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 7, 31, 12, 0);
        CreateScheduleDTO dto = new CreateScheduleDTO(tripItem, startTime, endTime);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> scheduleService.addSchedule(trip, dto));

        assertEquals("Schedule must be within the trip dates", exception.getMessage());
        verify(scheduleRepository, never()).save(any(Schedule.class));
    }

    @Test
    void addSchedule_shouldThrowException_whenEndTimeIsBeforeStartTime() {
        // Given
        Trip trip = new Trip();
        trip.setStartDate(LocalDate.of(2026, 8, 1));
        trip.setEndDate(LocalDate.of(2026, 8, 10));

        TripItem tripItem = new TripItem();
        LocalDateTime startTime = LocalDateTime.of(2026, 8, 2, 12, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 8, 2, 10, 0);
        CreateScheduleDTO dto = new CreateScheduleDTO(tripItem, startTime, endTime);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> scheduleService.addSchedule(trip, dto));

        assertEquals("End time must be after start time", exception.getMessage());
        verify(scheduleRepository, never()).save(any(Schedule.class));
    }

    @Test
    void addSchedule_shouldThrowException_whenStartAndEndTimesAreSame() {
        // Given
        Trip trip = new Trip();
        trip.setStartDate(LocalDate.of(2026, 8, 1));
        trip.setEndDate(LocalDate.of(2026, 8, 10));

        TripItem tripItem = new TripItem();
        LocalDateTime startTime = LocalDateTime.of(2026, 8, 2, 10, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 8, 2, 10, 0);
        CreateScheduleDTO dto = new CreateScheduleDTO(tripItem, startTime, endTime);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> scheduleService.addSchedule(trip, dto));

        assertEquals("Start time and end time cannot be the same", exception.getMessage());
        verify(scheduleRepository, never()).save(any(Schedule.class));
    }

    @Test
    void addSchedule_shouldThrowException_whenScheduleOverlapsWithExisting() {
        // Given
        Trip trip = new Trip();
        trip.setStartDate(LocalDate.of(2026, 8, 1));
        trip.setEndDate(LocalDate.of(2026, 8, 10));

        TripItem tripItem = new TripItem();
        LocalDateTime startTime = LocalDateTime.of(2026, 8, 2, 10, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 8, 2, 12, 0);
        CreateScheduleDTO dto = new CreateScheduleDTO(tripItem, startTime, endTime);

        when(scheduleRepository.existsOverlapping(startTime, endTime)).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> scheduleService.addSchedule(trip, dto));

        assertEquals("Schedule overlaps with an existing schedule", exception.getMessage());
        verify(scheduleRepository, times(1)).existsOverlapping(startTime, endTime);
        verify(scheduleRepository, never()).save(any(Schedule.class));
    }

    @Test
    void updateSchedule_shouldUpdateAndSave_whenDataIsValidAndTripItemIsDifferent() {
        UUID scheduleId = UUID.randomUUID();
        UUID existingItemId = UUID.randomUUID();
        UUID newItemId = UUID.randomUUID();

        Trip trip = new Trip();
        trip.setStartDate(LocalDate.of(2026, 8, 1));
        trip.setEndDate(LocalDate.of(2026, 8, 10));

        LocalDateTime startTime = LocalDateTime.of(2026, 8, 3, 10, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 8, 3, 12, 0);
        UpdateScheduleDTO dto = new UpdateScheduleDTO(scheduleId, newItemId, startTime, endTime);

        TripItem existingItem = new TripItem();
        existingItem.setId(existingItemId);

        Schedule existingSchedule = new Schedule();
        existingSchedule.setId(scheduleId);
        existingSchedule.setTripItem(existingItem);

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(existingSchedule));
        when(scheduleRepository.existsOverlapping(startTime, endTime)).thenReturn(false);
        when(scheduleRepository.save(any(Schedule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        scheduleService.updateSchedule(trip, dto);

        assertEquals(startTime, existingSchedule.getStartTime());
        assertEquals(endTime, existingSchedule.getEndTime());
        verify(scheduleRepository, times(1)).save(existingSchedule);
    }

    @Test
    void isDateValid_shouldThrowException_whenDatesAreOutsideTrip() {
        LocalDateTime tripStart = LocalDateTime.of(2026, 8, 1, 0, 0);
        LocalDateTime tripEnd = LocalDateTime.of(2026, 8, 10, 23, 59);

        LocalDateTime startTime = LocalDateTime.of(2026, 7, 31, 10, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 7, 31, 12, 0);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> scheduleService.isDateValid(startTime, endTime, tripStart, tripEnd));

        assertEquals("Schedule must be within the trip dates", exception.getMessage());
    }

    @Test
    void isDateValid_shouldThrowException_whenEndTimeIsBeforeStartTime() {
        LocalDateTime tripStart = LocalDateTime.of(2026, 8, 1, 0, 0);
        LocalDateTime tripEnd = LocalDateTime.of(2026, 8, 10, 23, 59);

        LocalDateTime startTime = LocalDateTime.of(2026, 8, 2, 12, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 8, 2, 10, 0);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> scheduleService.isDateValid(startTime, endTime, tripStart, tripEnd));

        assertEquals("End time must be after start time", exception.getMessage());
    }

    @Test
    void isDateValid_shouldThrowException_whenStartAndEndTimesAreSame() {
        LocalDateTime tripStart = LocalDateTime.of(2026, 8, 1, 0, 0);
        LocalDateTime tripEnd = LocalDateTime.of(2026, 8, 10, 23, 59);

        LocalDateTime startTime = LocalDateTime.of(2026, 8, 2, 10, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 8, 2, 10, 0);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> scheduleService.isDateValid(startTime, endTime, tripStart, tripEnd));

        assertEquals("Start time and end time cannot be the same", exception.getMessage());
    }

    @Test
    void isDateValid_shouldThrowException_whenScheduleOverlapsWithExisting() {
        LocalDateTime tripStart = LocalDateTime.of(2026, 8, 1, 0, 0);
        LocalDateTime tripEnd = LocalDateTime.of(2026, 8, 10, 23, 59);

        LocalDateTime startTime = LocalDateTime.of(2026, 8, 2, 10, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 8, 2, 12, 0);

        when(scheduleRepository.existsOverlapping(startTime, endTime)).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> scheduleService.isDateValid(startTime, endTime, tripStart, tripEnd));

        assertEquals("Schedule overlaps with an existing schedule", exception.getMessage());
    }

    @Test
    void deleteSchedule_shouldDeleteSuccessfully_whenExists() {
        // Given
        UUID id = UUID.randomUUID();
        Schedule schedule = new Schedule();
        schedule.setId(id);

        when(scheduleRepository.findById(id)).thenReturn(Optional.of(schedule));

        // When
        scheduleService.deleteSchedule(id);

        // Then
        verify(scheduleRepository, times(1)).findById(id);
        verify(scheduleRepository, times(1)).delete(schedule);
    }
}