package net.dysky.planner.trip;

import net.dysky.planner.exception.TripNotFoundException;
import net.dysky.planner.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripServiceTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private TripService tripService;

    @Test
    void getTripById_shouldReturnTrip_whenTripExists() {
        // Given
        UUID id = UUID.randomUUID();
        Trip expectedTrip = new Trip();
        expectedTrip.setId(id);

        when(tripRepository.findById(id)).thenReturn(Optional.of(expectedTrip));

        // When
        Trip actualTrip = tripService.getTripById(id);

        // Then
        assertNotNull(actualTrip);
        assertEquals(expectedTrip, actualTrip);
        verify(tripRepository, times(1)).findById(id);
    }

    @Test
    void getTripById_shouldThrowTripNotFoundException_whenTripDoesNotExist() {
        // Given
        UUID id = UUID.randomUUID();
        when(tripRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(TripNotFoundException.class, () -> tripService.getTripById(id));
        verify(tripRepository, times(1)).findById(id);
    }

    @Test
    void getAllTrips_shouldReturnListOfTrips() {
        // Given
        List<Trip> expectedTrips = List.of(new Trip(), new Trip());
        when(tripRepository.findAll()).thenReturn(expectedTrips);

        // When
        List<Trip> actualTrips = tripService.getAllTrips();

        // Then
        assertEquals(expectedTrips.size(), actualTrips.size());
        verify(tripRepository, times(1)).findAll();
    }

    @Test
    void getTripsByStatus_shouldReturnTripsWithGivenStatus() {
        // Given
        TripStatus status = TripStatus.PLANNED;
        List<Trip> expectedTrips = List.of(new Trip());
        when(tripRepository.findByStatus(status)).thenReturn(expectedTrips);

        // When
        List<Trip> actualTrips = tripService.getTripsByStatus(status);

        // Then
        assertEquals(expectedTrips.size(), actualTrips.size());
        verify(tripRepository, times(1)).findByStatus(status);
    }

    @Test
    void createTrip_shouldSaveAndReturnTrip_whenBudgetIsPositive() {
        // Given
        String email = " test.test@planner.com";
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(7);
        CreateTripDTO dto = new CreateTripDTO("Wycieczka do Rzymu", "Rzym", 2500.0, startDate, endDate);

        ArgumentCaptor<Trip> tripCaptor = ArgumentCaptor.forClass(Trip.class);
        when(tripRepository.save(any(Trip.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Trip createdTrip = tripService.createTrip(dto, email);

        // Then
        assertNotNull(createdTrip);
        verify(tripRepository, times(1)).save(tripCaptor.capture());

        Trip savedTrip = tripCaptor.getValue();
        assertEquals("Wycieczka do Rzymu", savedTrip.getName());
        assertEquals("Rzym", savedTrip.getDestination());
        assertEquals(TripStatus.PLANNED, savedTrip.getStatus());
        assertNull(savedTrip.getTripCreator());
        assertEquals(2500.0, savedTrip.getBudget());
        assertEquals(startDate, savedTrip.getStartDate());
        assertEquals(endDate, savedTrip.getEndDate());
    }

    @Test
    void createTrip_shouldThrowRuntimeException_whenBudgetIsZeroOrNegative() {
        // Given
        String email = " test.test@planner.com";
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(7);
        CreateTripDTO dtoWithZeroBudget = new CreateTripDTO("Wycieczka", "Rzym", 0.0, startDate, endDate);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> tripService.createTrip(dtoWithZeroBudget, email));
        assertEquals("Budget must be a positive value", exception.getMessage());

        verify(tripRepository, never()).save(any(Trip.class));
    }
}