package net.dysky.planner.tripSchedule;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface TripScheduleRepository extends JpaRepository<TripSchedule, TripScheduleId> {
    List<TripSchedule> findAllByTripId(UUID tripId);
}
