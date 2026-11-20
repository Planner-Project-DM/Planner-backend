package net.dysky.planner.trip;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import net.dysky.planner.AbstractIntegrationTest;
import net.dysky.planner.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class TripNotesRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private TripNotesRepository tripNotesRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void save_shouldPersistLargeContentAndInitializeVersion() {
        Trip trip = persistTrip(persistUser("notes@example.com"));
        String largeContent = "{\"doc\":\"" + "x".repeat(2 * 1024 * 1024) + "\"}";

        TripNotes notes = new TripNotes();
        notes.setTripId(trip.getId());
        notes.setContent(largeContent);
        notes.setUpdatedAt(LocalDateTime.now());
        notes.setUpdatedBy("notes@example.com");
        tripNotesRepository.save(notes);

        entityManager.flush();
        entityManager.clear();

        Optional<TripNotes> found = tripNotesRepository.findById(trip.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getContent()).isEqualTo(largeContent);
        assertThat(found.get().getVersion()).isZero();
        assertThat(found.get().getUpdatedBy()).isEqualTo("notes@example.com");
    }

    @Test
    void update_shouldIncrementVersion() {
        Trip trip = persistTrip(persistUser("notes@example.com"));

        TripNotes notes = new TripNotes();
        notes.setTripId(trip.getId());
        notes.setContent("{}");
        notes.setUpdatedAt(LocalDateTime.now());
        tripNotesRepository.save(notes);
        entityManager.flush();
        entityManager.clear();

        TripNotes loaded = tripNotesRepository.findById(trip.getId()).orElseThrow();
        loaded.setContent("{\"shapes\":[]}");
        loaded.setUpdatedAt(LocalDateTime.now());
        entityManager.flush();
        entityManager.clear();

        TripNotes reloaded = tripNotesRepository.findById(trip.getId()).orElseThrow();
        assertThat(reloaded.getVersion()).isEqualTo(1L);
        assertThat(reloaded.getContent()).isEqualTo("{\"shapes\":[]}");
    }

    private User persistUser(String email) {
        User user = new User();
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail(email);
        user.setPassword("secret");
        user.setPhoneNumber("123456789");
        entityManager.persist(user);
        return user;
    }

    private Trip persistTrip(User creator) {
        Trip trip = new Trip();
        trip.setName("Notes trip");
        trip.setDestination("Destination");
        trip.setStatus(TripStatus.PLANNED);
        trip.setTripCreator(creator);
        trip.setBudget(1000.0);
        trip.setStartDate(LocalDate.now());
        trip.setEndDate(LocalDate.now().plusDays(7));
        entityManager.persist(trip);
        return trip;
    }
}
