package net.dysky.planner.tripitinerary;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TripTripItemsId implements Serializable {

    UUID trip;
    UUID tripItem;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TripTripItemsId that = (TripTripItemsId) obj;
        return trip.equals(that.trip) && tripItem.equals(that.tripItem);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(trip, tripItem);
    }
}
