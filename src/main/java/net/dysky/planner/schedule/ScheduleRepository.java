package net.dysky.planner.schedule;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.UUID;

interface ScheduleRepository extends JpaRepository<Schedule, UUID> {

    @Query("SELECT COUNT(s) > 0 FROM Schedule s WHERE s.startTime < :endTime AND s.endTime > :startTime")
    boolean existsOverlapping(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Query("SELECT COUNT(s) > 0 FROM Schedule s WHERE s.startTime < :endTime AND s.endTime > :startTime AND s.id <> :scheduleId")
    boolean existsOverlapping(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime, @Param("scheduleId") UUID scheduleId);

    @Query("""
        SELECT COUNT(ts) > 0
        FROM TripSchedule ts
        JOIN ts.schedule s
        WHERE ts.trip.id = :tripId
          AND s.startTime < :endTime
          AND s.endTime > :startTime
          AND (:scheduleId IS NULL OR s.id <> :scheduleId)
        """)
    boolean existsOverlapping(
            @Param("tripId") UUID tripId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("scheduleId") UUID scheduleId
    );
}
