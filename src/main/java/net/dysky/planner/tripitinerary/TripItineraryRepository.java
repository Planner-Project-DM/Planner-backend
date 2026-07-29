package net.dysky.planner.tripitinerary;

import org.springframework.data.jpa.repository.JpaRepository;

interface TripItineraryRepository extends JpaRepository<TripItinerary, TripTripItemsId> {

}
