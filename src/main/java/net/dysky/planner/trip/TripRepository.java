package net.dysky.planner.trip;

import net.dysky.planner.group.Group;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface TripRepository extends JpaRepository<Trip, UUID> {

    @EntityGraph(attributePaths = {"tripGroup", "tripCreator", "tripItineraries"})
    Optional<Trip> findById(UUID id);

    @Query("SELECT t FROM Trip t " +
            "LEFT JOIN FETCH t.tripGroup " +
            "LEFT JOIN FETCH t.tripItineraries " +
            "WHERE t.id = :id")
    Optional<Trip> findByIdWithDetails(@Param("id") UUID id);

    List<Trip> findByStatus(TripStatus status);

    @EntityGraph(attributePaths = {"tripGroup", "tripCreator"})
    List<Trip> findAllByTripCreatorEmail(String tripCreatorEmail);

    @EntityGraph(attributePaths = {"tripGroup", "tripCreator"})
    List<Trip> findAllByTripCreatorEmailAndStatus(String tripCreatorEmail, TripStatus status);

    List<Trip> findAllByTripGroupIn(Collection<Group> groups);

    boolean existsByNameAndTripCreator_Email(String name, String tripCreatorEmail);

    List<Trip> findAllByTripGroup(Group tripGroup);
}
