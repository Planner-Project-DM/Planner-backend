package net.dysky.planner.tripitinerary;

import net.dysky.planner.trip.Trip;
import net.dysky.planner.tripitem.TripItem;
import net.dysky.planner.tripitem.TripItemService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @InjectMocks
    private TripItineraryService tripItineraryService;

    @Test
    void findById_shouldReturnItinerary_whenExists() {
        // Given
        UUID tripId = UUID.randomUUID();
        UUID tripItemId = UUID.randomUUID();
        TripItinerary expected = new TripItinerary();

        when(tripItineraryRepository.findById(any(TripTripItemsId.class))).thenReturn(Optional.of(expected));

        // When
        TripItinerary result = tripItineraryService.findById(tripId, tripItemId);

        // Then
        assertNotNull(result);
        assertEquals(expected, result);
        verify(tripItineraryRepository, times(1)).findById(any(TripTripItemsId.class));
    }

    @Test
    void findById_shouldThrowRuntimeException_whenNotFound() {
        // Given
        UUID tripId = UUID.randomUUID();
        UUID tripItemId = UUID.randomUUID();

        when(tripItineraryRepository.findById(any(TripTripItemsId.class))).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> tripItineraryService.findById(tripId, tripItemId));

        assertEquals("Trip Itinerary not found", exception.getMessage());
        verify(tripItineraryRepository, times(1)).findById(any(TripTripItemsId.class));
    }

    @Test
    void getTotalCostByTripId_shouldReturnSumOfPrices() {
        // Given
        UUID tripId = UUID.randomUUID();
        TripItinerary iti1 = new TripItinerary();
        iti1.setPrice(150.0);
        TripItinerary iti2 = new TripItinerary();
        iti2.setPrice(250.50);

        when(tripItineraryRepository.findAllByTripId(tripId)).thenReturn(List.of(iti1, iti2));

        // When
        Double totalCost = tripItineraryService.getTotalCostByTripId(tripId);

        // Then
        assertEquals(400.50, totalCost);
        verify(tripItineraryRepository, times(1)).findAllByTripId(tripId);
    }

    @Test
    void addTripItinerary_shouldSaveAndReturnItinerary() {
        // Given
        Trip trip = new Trip();
        UUID tripItemId = UUID.randomUUID();
        CreateTripItineraryDTO dto = new CreateTripItineraryDTO(tripItemId);

        TripItem tripItem = new TripItem();
        when(tripItemService.findById(tripItemId)).thenReturn(tripItem);
        when(tripItineraryRepository.save(any(TripItinerary.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        TripItinerary result = tripItineraryService.addTripItinerary(trip, dto);

        // Then
        assertNotNull(result);
        assertEquals(trip, result.getTrip());
        assertEquals(tripItem, result.getTripItem());
        assertEquals(0.0, result.getPrice());

        verify(tripItemService, times(1)).findById(tripItemId);
        verify(tripItineraryRepository, times(1)).save(any(TripItinerary.class));
    }

    @Test
    void updateTripItinerary_shouldUpdatePriceAndSave() {
        // Given
        UUID tripId = UUID.randomUUID();
        UUID tripItemId = UUID.randomUUID();
        Trip trip = new Trip();
        trip.setId(tripId);

        UpdateTripItineraryDTO dto = new UpdateTripItineraryDTO(tripItemId, 350.0);

        TripItinerary existingItinerary = new TripItinerary();
        existingItinerary.setPrice(0.0);

        when(tripItineraryRepository.findById(any(TripTripItemsId.class))).thenReturn(Optional.of(existingItinerary));
        when(tripItineraryRepository.save(any(TripItinerary.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        TripItinerary result = tripItineraryService.updateTripItinerary(trip, dto);

        // Then
        assertNotNull(result);
        assertEquals(350.0, result.getPrice());

        verify(tripItineraryRepository, times(1)).findById(any(TripTripItemsId.class));
        verify(tripItineraryRepository, times(1)).save(any(TripItinerary.class));
    }

    @Test
    void deleteTripItinerary_shouldDeleteSuccessfully() {
        // Given
        UUID tripId = UUID.randomUUID();
        UUID tripItemId = UUID.randomUUID();

        // When
        tripItineraryService.deleteTripItinerary(tripId, tripItemId);

        // Then
        verify(tripItineraryRepository, times(1)).deleteById(any(TripTripItemsId.class));
    }
}