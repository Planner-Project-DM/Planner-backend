package net.dysky.planner.schedule;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.UUID;

interface ScheduleRepository extends JpaRepository<Schedule, UUID> {

    @Query("SELECT COUNT(s) > 0 FROM Schedule s WHERE s.startTime < :endTime AND s.endTime > :startTime")
    boolean existsOverlapping(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}
