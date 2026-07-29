package net.dysky.planner.tripitinerary;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import net.dysky.planner.trip.Trip;
import net.dysky.planner.tripItem.TripItem;

@Getter
@Setter
@Entity
@IdClass(TripTripItemsId.class)
@Table(name = "trip_itineraries")
public class TripItinerary {

    @Id
    @ManyToOne
    @JoinColumn(name = "trip_id")
    @JsonIgnoreProperties("tripItineraries")
    private Trip trip;

    @Id
    @ManyToOne
    @JoinColumn(name = "trip_item_id")
    @JsonIgnoreProperties("tripItineraries")
    private TripItem tripItem;

    private Double price;

}
