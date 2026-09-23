package net.dysky.planner.tripitinerary;

import net.dysky.planner.exception.TripFoundException;
import net.dysky.planner.group.Group;
import net.dysky.planner.groupUser.GroupRole;
import net.dysky.planner.groupUser.GroupUser;
import net.dysky.planner.notification.NotificationService;
import net.dysky.planner.trip.Trip;
import net.dysky.planner.tripitem.TripItem;
import net.dysky.planner.tripitem.TripItemService;
import net.dysky.planner.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripItineraryServiceTest {

    @Mock
    private TripItineraryRepository tripItineraryRepository;

    @Mock
    private TripItemService tripItemService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private TripItineraryService tripItineraryService;

    @Test
    void findById_shouldReturnItinerary_whenExists() {
        UUID tripId = UUID.randomUUID();
        UUID tripItemId = UUID.randomUUID();
        TripItinerary expected = new TripItinerary();

        when(tripItineraryRepository.findById(any(TripTripItemsId.class))).thenReturn(Optional.of(expected));

        TripItinerary result = tripItineraryService.findById(tripId, tripItemId);

        assertNotNull(result);
        assertEquals(expected, result);
        verify(tripItineraryRepository, times(1)).findById(any(TripTripItemsId.class));
    }

    @Test
    void findById_shouldThrowRuntimeException_whenNotFound() {
        UUID tripId = UUID.randomUUID();
        UUID tripItemId = UUID.randomUUID();

        when(tripItineraryRepository.findById(any(TripTripItemsId.class))).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> tripItineraryService.findById(tripId, tripItemId));

        assertEquals("Trip Itinerary not found", exception.getMessage());
        verify(tripItineraryRepository, times(1)).findById(any(TripTripItemsId.class));
    }

    @Test
    void existsByTripItem_shouldReturnTrue_whenExists() {
        Trip trip = new Trip();
        UUID tripItemId = UUID.randomUUID();

        when(tripItineraryRepository.existsByTripAndTripItem_Id(trip, tripItemId)).thenReturn(true);

        boolean exists = tripItineraryService.existsByTripItem(trip, tripItemId);

        assertTrue(exists);
        verify(tripItineraryRepository, times(1)).existsByTripAndTripItem_Id(trip, tripItemId);
    }

    @Test
    void existsByTripItem_shouldReturnFalse_whenNotExists() {
        Trip trip = new Trip();
        UUID tripItemId = UUID.randomUUID();

        when(tripItineraryRepository.existsByTripAndTripItem_Id(trip, tripItemId)).thenReturn(false);

        boolean exists = tripItineraryService.existsByTripItem(trip, tripItemId);

        assertFalse(exists);
        verify(tripItineraryRepository, times(1)).existsByTripAndTripItem_Id(trip, tripItemId);
    }

    @Test
    void getTotalCostByTripId_shouldReturnSumOfPrices() {
        UUID tripId = UUID.randomUUID();
        TripItinerary iti1 = new TripItinerary();
        iti1.setPrice(150.0);
        TripItinerary iti2 = new TripItinerary();
        iti2.setPrice(250.50);

        when(tripItineraryRepository.findAllByTripId(tripId)).thenReturn(List.of(iti1, iti2));

        Double totalCost = tripItineraryService.getTotalCostByTripId(tripId);

        assertEquals(400.50, totalCost);
        verify(tripItineraryRepository, times(1)).findAllByTripId(tripId);
    }

    @Test
    void addTripItinerary_shouldSaveAndReturnItinerary_whenNotExists() {
        Trip trip = new Trip();
        UUID tripItemId = UUID.randomUUID();
        CreateTripItineraryDTO dto = new CreateTripItineraryDTO(tripItemId);

        TripItem tripItem = new TripItem();
        tripItem.setId(tripItemId);

        when(tripItemService.findById(tripItemId)).thenReturn(tripItem);
        when(tripItineraryRepository.existsByTripAndTripItem_Id(trip, tripItemId)).thenReturn(false);
        when(tripItineraryRepository.save(any(TripItinerary.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TripItinerary result = tripItineraryService.addTripItinerary(trip, dto);

        assertNotNull(result);
        assertEquals(trip, result.getTrip());
        assertEquals(tripItem, result.getTripItem());
        assertEquals(0.0, result.getPrice());

        verify(tripItemService, times(1)).findById(tripItemId);
        verify(tripItineraryRepository, times(1)).save(any(TripItinerary.class));
    }

    @Test
    void addTripItinerary_shouldThrowTripFoundException_whenItemAlreadyExists() {
        Trip trip = new Trip();
        UUID tripItemId = UUID.randomUUID();
        CreateTripItineraryDTO dto = new CreateTripItineraryDTO(tripItemId);

        TripItem tripItem = new TripItem();
        tripItem.setId(tripItemId);

        when(tripItemService.findById(tripItemId)).thenReturn(tripItem);
        when(tripItineraryRepository.existsByTripAndTripItem_Id(trip, tripItemId)).thenReturn(true);

        TripFoundException exception = assertThrows(TripFoundException.class,
                () -> tripItineraryService.addTripItinerary(trip, dto));

        assertEquals("Trip Item already exists in the itinerary", exception.getMessage());
        verify(tripItineraryRepository, never()).save(any(TripItinerary.class));
    }

    @Test
    void updateTripItinerary_shouldUpdatePriceAndNotifyNonOwnerUsers() {
        UUID tripId = UUID.randomUUID();
        UUID tripItemId = UUID.randomUUID();

        TripItem tripItem = new TripItem();
        tripItem.setName("Eiffel Tower");

        TripItinerary existingItinerary = new TripItinerary();
        existingItinerary.setPrice(100.0);
        existingItinerary.setTripItem(tripItem);

        User ownerUser = new User();
        ownerUser.setId(UUID.randomUUID());

        GroupUser owner = new GroupUser();
        owner.setRole(GroupRole.OWNER);
        owner.setUser(ownerUser);

        User memberUser = new User();
        UUID memberId = UUID.randomUUID();
        memberUser.setId(memberId);

        GroupUser member = new GroupUser();
        member.setRole(GroupRole.MEMBER);
        member.setUser(memberUser);

        Group group = new Group();
        group.setGroupUsers(new ArrayList<>(List.of(owner, member)));

        Trip trip = new Trip();
        trip.setId(tripId);
        trip.setTripGroup(group);

        UpdateTripItineraryDTO dto = new UpdateTripItineraryDTO(tripItemId, 350.0);

        when(tripItineraryRepository.findById(any(TripTripItemsId.class))).thenReturn(Optional.of(existingItinerary));
        when(tripItineraryRepository.save(any(TripItinerary.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TripItinerary result = tripItineraryService.updateTripItinerary(trip, dto);

        assertNotNull(result);
        assertEquals(350.0, result.getPrice());

        verify(notificationService, times(1)).createNotification(
                eq("Cost of item updated"),
                eq("The cost of item Eiffel Tower has been updated to 350.0"),
                eq(memberId)
        );
        verify(notificationService, never()).createNotification(any(), any(), eq(ownerUser.getId()));
        verify(tripItineraryRepository, times(1)).save(existingItinerary);
    }

    @Test
    void deleteTripItinerary_shouldDeleteSuccessfully() {
        UUID tripId = UUID.randomUUID();
        UUID tripItemId = UUID.randomUUID();

        tripItineraryService.deleteTripItinerary(tripId, tripItemId);

        verify(tripItineraryRepository, times(1)).deleteById(any(TripTripItemsId.class));
    }
}