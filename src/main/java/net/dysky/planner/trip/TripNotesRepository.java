package net.dysky.planner.trip;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface TripNotesRepository extends JpaRepository<TripNotes, UUID> {
}
