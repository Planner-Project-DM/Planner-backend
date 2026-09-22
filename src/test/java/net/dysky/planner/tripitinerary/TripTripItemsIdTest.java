package net.dysky.planner.tripitinerary;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TripTripItemsIdTest {

    @Test
    void shouldTestGettersSettersEqualsAndHashCode() {
        UUID tripId1 = UUID.randomUUID();
        UUID itemId1 = UUID.randomUUID();

        TripTripItemsId id1 = new TripTripItemsId(tripId1, itemId1);
        TripTripItemsId id2 = new TripTripItemsId(tripId1, itemId1);
        TripTripItemsId id3 = new TripTripItemsId(UUID.randomUUID(), itemId1);
        TripTripItemsId idEmpty = new TripTripItemsId();

        idEmpty.setTrip(tripId1);
        idEmpty.setTripItem(itemId1);

        assertEquals(tripId1, idEmpty.getTrip());
        assertEquals(itemId1, idEmpty.getTripItem());

        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());

        assertNotEquals(id1, id3);
        assertNotEquals(id1, null);
        assertNotEquals(id1, new Object());
    }
}