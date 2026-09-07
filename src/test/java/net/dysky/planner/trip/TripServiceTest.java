package net.dysky.planner.trip;

import net.dysky.planner.exception.TripNotFoundException;
import net.dysky.planner.group.Group;
import net.dysky.planner.group.GroupService;
import net.dysky.planner.notification.NotificationService;
import net.dysky.planner.tripitem.TripItem;
import net.dysky.planner.tripitem.TripItemService;
import net.dysky.planner.tripitinerary.TripItineraryService;
import net.dysky.planner.user.User;
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

    @Mock
    private GroupService groupService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private TripItemService tripItemService;

    @Mock
    private TripItineraryService tripItineraryService;

    @InjectMocks
    private TripService tripService;

    @Test
    void getTripById_shouldReturnTrip_whenTripExists() {
        UUID id = UUID.randomUUID();
        Trip expectedTrip = new Trip();
        expectedTrip.setId(id);

        when(tripRepository.findById(id)).thenReturn(Optional.of(expectedTrip));

        Trip actualTrip = tripService.getTripById(id);

        assertNotNull(actualTrip);
        assertEquals(expectedTrip, actualTrip);
        verify(tripRepository, times(1)).findById(id);
    }

    @Test
    void getTripById_shouldThrowTripNotFoundException_whenTripDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(tripRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(TripNotFoundException.class, () -> tripService.getTripById(id));
        verify(tripRepository, times(1)).findById(id);
    }

    @Test
    void getAllTrips_shouldReturnListOfTrips() {
        List<Trip> expectedTrips = List.of(new Trip(), new Trip());
        when(tripRepository.findAll()).thenReturn(expectedTrips);

        List<Trip> actualTrips = tripService.getAllTrips();

        assertEquals(expectedTrips.size(), actualTrips.size());
        verify(tripRepository, times(1)).findAll();
    }

    @Test
    void getTripsByStatus_shouldReturnTripsWithGivenStatus() {
        TripStatus status = TripStatus.PLANNED;
        List<Trip> expectedTrips = List.of(new Trip());
        when(tripRepository.findByStatus(status)).thenReturn(expectedTrips);

        List<Trip> actualTrips = tripService.getTripsByStatus(status);

        assertEquals(expectedTrips.size(), actualTrips.size());
        verify(tripRepository, times(1)).findByStatus(status);
    }

    @Test
    void getAllTripsByEmail_shouldReturnListOfTrips() {
        String email = "test@example.com";
        List<Trip> expectedTrips = List.of(new Trip());
        when(tripRepository.findAllByTripCreatorEmail(email)).thenReturn(expectedTrips);

        List<Trip> actualTrips = tripService.getAllTripsByEmail(email);

        assertEquals(expectedTrips.size(), actualTrips.size());
        verify(tripRepository, times(1)).findAllByTripCreatorEmail(email);
    }

    @Test
    void getAllTripsByEmailAndStatus_shouldReturnListOfTrips() {
        String email = "test@example.com";
        TripStatus status = TripStatus.PLANNED;
        List<Trip> expectedTrips = List.of(new Trip());
        when(tripRepository.findAllByTripCreatorEmailAndStatus(email, status)).thenReturn(expectedTrips);

        List<Trip> actualTrips = tripService.getAllTripsByEmailAndStatus(email, status);

        assertEquals(expectedTrips.size(), actualTrips.size());
        verify(tripRepository, times(1)).findAllByTripCreatorEmailAndStatus(email, status);
    }

    @Test
    void updateTripCosts_shouldSaveAndReturnTrip_whenTotalCostDoesNotExceedBudget() {
        UUID tripId = UUID.randomUUID();
        Trip trip = new Trip();
        trip.setId(tripId);
        trip.setBudget(1000.0);

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(tripItineraryService.getTotalCostByTripId(tripId)).thenReturn(800.0);
        when(tripRepository.save(any(Trip.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Trip result = tripService.updateTripCosts(tripId);

        assertNotNull(result);
        assertEquals(800.0, result.getActualCost());
        verify(tripRepository, times(1)).save(trip);
    }

    @Test
    void updateTripCosts_shouldThrowException_whenTotalCostExceedsBudget() {
        UUID tripId = UUID.randomUUID();
        Trip trip = new Trip();
        trip.setId(tripId);
        trip.setBudget(500.0);

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(tripItineraryService.getTotalCostByTripId(tripId)).thenReturn(800.0);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> tripService.updateTripCosts(tripId));
        assertEquals("Total cost exceeds budget", exception.getMessage());
        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void createTrip_shouldSaveAndReturnTrip_whenBudgetIsPositive() {
        User user = mock(User.class);

        String email = "test.test@planner.com";
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(7);
        CreateTripDTO dto = new CreateTripDTO("Wycieczka do Rzymu", "Rzym", 2500.0, startDate, endDate);

        ArgumentCaptor<Trip> tripCaptor = ArgumentCaptor.forClass(Trip.class);
        when(tripRepository.save(any(Trip.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(user.getId()).thenReturn(UUID.randomUUID());
        when(userService.getUserByEmail("test.test@planner.com")).thenReturn(user);

        Trip createdTrip = tripService.createTrip(dto, email);

        assertNotNull(createdTrip);
        verify(tripRepository, times(1)).save(tripCaptor.capture());

        Trip savedTrip = tripCaptor.getValue();
        assertEquals("Wycieczka do Rzymu", savedTrip.getName());
        assertEquals("Rzym", savedTrip.getDestination());
        assertEquals(TripStatus.PLANNED, savedTrip.getStatus());
        assertNotNull(savedTrip.getTripCreator());
        assertEquals(2500.0, savedTrip.getBudget());
        assertEquals(startDate, savedTrip.getStartDate());
        assertEquals(endDate, savedTrip.getEndDate());
    }

    @Test
    void createTrip_shouldThrowRuntimeException_whenBudgetIsZeroOrNegative() {
        String email = "test.test@planner.com";
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(7);
        CreateTripDTO dtoWithZeroBudget = new CreateTripDTO("Wycieczka", "Rzym", 0.0, startDate, endDate);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> tripService.createTrip(dtoWithZeroBudget, email));
        assertEquals("Budget must be a positive value", exception.getMessage());

        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void updateTrip_shouldUpdateAllFields_whenDtoHasAllFields() {
        UUID id = UUID.randomUUID();
        Trip trip = new Trip();
        trip.setId(id);
        trip.setName("Old Name");

        Group newGroup = new Group();
        UpdateTripDTO dto = new UpdateTripDTO(
                "New Name",
                "New Destination",
                TripStatus.COMPLETED,
                1500.0,
                LocalDate.now(),
                LocalDate.now().plusDays(5),
                newGroup
        );

        when(tripRepository.findById(id)).thenReturn(Optional.of(trip));
        when(tripRepository.save(any(Trip.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Trip result = tripService.updateTrip(id, dto);

        assertNotNull(result);
        assertEquals("New Name", result.getName());
        assertEquals("New Destination", result.getDestination());
        assertEquals(TripStatus.COMPLETED, result.getStatus());
        assertEquals(1500.0, result.getBudget());
        assertEquals(newGroup, result.getTripGroup());
        verify(tripRepository, times(1)).save(trip);
    }

    @Test
    void updateTrip_shouldNotUpdateFields_whenDtoHasNullFields() {
        UUID id = UUID.randomUUID();
        Trip trip = new Trip();
        trip.setId(id);
        trip.setName("Old Name");
        trip.setBudget(1000.0);

        UpdateTripDTO dto = new UpdateTripDTO(null, null, null, null, null, null, null);

        when(tripRepository.findById(id)).thenReturn(Optional.of(trip));
        when(tripRepository.save(any(Trip.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Trip result = tripService.updateTrip(id, dto);

        assertNotNull(result);
        assertEquals("Old Name", result.getName());
        assertEquals(1000.0, result.getBudget());
        verify(tripRepository, times(1)).save(trip);
    }

    @Test
    void deleteTrip_shouldDeleteSuccessfully() {
        UUID id = UUID.randomUUID();
        Trip trip = new Trip();
        trip.setId(id);

        when(tripRepository.findById(id)).thenReturn(Optional.of(trip));

        tripService.deleteTrip(id);

        verify(tripRepository, times(1)).delete(trip);
    }

    @Test
    void removeTripItemFromTrip_shouldRemoveAndRecalculate() {
        UUID tripId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        String name = "Item Name";

        Trip trip = new Trip();
        trip.setId(tripId);
        trip.setBudget(1000.0);

        TripItem tripItem = new TripItem();
        tripItem.setId(itemId);

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(tripItemService.findByName(name)).thenReturn(tripItem);
        when(tripItineraryService.getTotalCostByTripId(tripId)).thenReturn(200.0);
        when(tripRepository.save(any(Trip.class))).thenAnswer(invocation -> invocation.getArgument(0));

        tripService.removeTripItemFromTrip(tripId, name);

        verify(tripItineraryService, times(1)).deleteTripItinerary(tripId, itemId);
        verify(tripRepository, times(1)).save(trip);
    }
}