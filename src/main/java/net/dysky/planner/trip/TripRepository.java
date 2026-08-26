package net.dysky.planner.trip;

import net.dysky.planner.group.Group;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface TripRepository extends JpaRepository<Trip, UUID> {
    List<Trip> findByStatus(TripStatus status);

    List<Trip> findAllByTripCreatorEmail(String tripCreatorEmail);

    List<Trip> findAllByTripCreatorEmailAndStatus(String tripCreatorEmail, TripStatus status);

    boolean existsByNameAndTripCreator_Email(String name, String tripCreatorEmail);

    List<Trip> findAllByTripGroup(Group tripGroup);
}
