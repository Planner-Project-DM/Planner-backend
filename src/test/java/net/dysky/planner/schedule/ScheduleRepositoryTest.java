package net.dysky.planner.schedule;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import net.dysky.planner.trip.Trip;
import net.dysky.planner.tripSchedule.TripSchedule;
import net.dysky.planner.tripitem.TripItem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ScheduleRepositoryTest {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void existsOverlapping_shouldReturnTrue_whenScheduleIntervalsOverlap() {
        persistSchedule(
                LocalDateTime.of(2026, 8, 2, 10, 0),
                LocalDateTime.of(2026, 8, 2, 12, 0)
        );

        entityManager.flush();
        entityManager.clear();

        assertThat(scheduleRepository.existsOverlapping(
                LocalDateTime.of(2026, 8, 2, 11, 0),
                LocalDateTime.of(2026, 8, 2, 13, 0)
        )).isTrue();
    }

    @Test
    void existsOverlapping_shouldReturnFalse_whenScheduleIntervalsOnlyTouchAtBoundary() {
        persistSchedule(
                LocalDateTime.of(2026, 8, 2, 10, 0),
                LocalDateTime.of(2026, 8, 2, 12, 0)
        );

        entityManager.flush();
        entityManager.clear();

        assertThat(scheduleRepository.existsOverlapping(
                LocalDateTime.of(2026, 8, 2, 12, 0),
                LocalDateTime.of(2026, 8, 2, 13, 0)
        )).isFalse();
    }

    @Test
    void existsOverlapping_shouldBeScopedToTrip() {
        Trip tripOne = new Trip();
        tripOne.setName("Trip A");
        entityManager.persist(tripOne);

        Trip tripTwo = new Trip();
        tripTwo.setName("Trip B");
        entityManager.persist(tripTwo);

        Schedule schedule = persistSchedule(
                LocalDateTime.of(2026, 8, 2, 10, 0),
                LocalDateTime.of(2026, 8, 2, 12, 0)
        );
        persistTripSchedule(tripOne, schedule);

        entityManager.flush();
        entityManager.clear();

        assertThat(scheduleRepository.existsOverlapping(
                tripTwo.getId(),
                LocalDateTime.of(2026, 8, 2, 11, 0),
                LocalDateTime.of(2026, 8, 2, 13, 0),
                null
        )).isFalse();
    }

    @Test
    void existsOverlapping_shouldReturnFalse_whenScheduleIntervalsDoNotOverlap() {
        persistSchedule(
                LocalDateTime.of(2026, 8, 2, 10, 0),
                LocalDateTime.of(2026, 8, 2, 12, 0)
        );

        entityManager.flush();
        entityManager.clear();

        assertThat(scheduleRepository.existsOverlapping(
                LocalDateTime.of(2026, 8, 2, 13, 0),
                LocalDateTime.of(2026, 8, 2, 14, 0)
        )).isFalse();
    }

    private Schedule persistSchedule(LocalDateTime startTime, LocalDateTime endTime) {
        TripItem tripItem = new TripItem();
        tripItem.setName("Test item");
        entityManager.persist(tripItem);

        Schedule schedule = new Schedule();
        schedule.setTripItem(tripItem);
        schedule.setStartTime(startTime);
        schedule.setEndTime(endTime);
        entityManager.persist(schedule);

        return schedule;
    }

    private void persistTripSchedule(Trip trip, Schedule schedule) {
        TripSchedule tripSchedule = new TripSchedule();
        tripSchedule.setTrip(trip);
        tripSchedule.setSchedule(schedule);
        entityManager.persist(tripSchedule);
    }
}
