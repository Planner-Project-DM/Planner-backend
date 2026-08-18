package net.dysky.planner.trip;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import net.dysky.planner.group.Group;
import net.dysky.planner.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TripRepositoryTest {

    @Autowired
    private TripRepository tripRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void findByStatus_shouldReturnOnlyMatchingTrips() {
        User creator = persistUser("creator@example.com");
        persistTrip("Paris", creator, TripStatus.PLANNED);
        persistTrip("Rome", creator, TripStatus.COMPLETED);

        entityManager.flush();
        entityManager.clear();

        List<Trip> trips = tripRepository.findByStatus(TripStatus.PLANNED);

        assertThat(trips)
                .hasSize(1)
                .extracting(Trip::getName)
                .containsExactly("Paris");
    }

    @Test
    void findAllByTripCreatorEmail_shouldReturnTripsForGivenCreator() {
        User creator = persistUser("creator@example.com");
        User otherCreator = persistUser("other@example.com");

        persistTrip("Paris", creator, TripStatus.PLANNED);
        persistTrip("Rome", creator, TripStatus.COMPLETED);
        persistTrip("Berlin", otherCreator, TripStatus.PLANNED);

        entityManager.flush();
        entityManager.clear();

        List<Trip> trips = tripRepository.findAllByTripCreatorEmail("creator@example.com");

        assertThat(trips)
                .hasSize(2)
                .extracting(Trip::getName)
                .containsExactlyInAnyOrder("Paris", "Rome");
    }

    @Test
    void findAllByTripCreatorEmailAndStatus_shouldFilterByCreatorAndStatus() {
        User creator = persistUser("creator@example.com");
        User otherCreator = persistUser("other@example.com");

        persistTrip("Paris", creator, TripStatus.PLANNED);
        persistTrip("Rome", creator, TripStatus.COMPLETED);
        persistTrip("Berlin", otherCreator, TripStatus.PLANNED);

        entityManager.flush();
        entityManager.clear();

        List<Trip> trips = tripRepository.findAllByTripCreatorEmailAndStatus("creator@example.com", TripStatus.PLANNED);

        assertThat(trips)
                .hasSize(1)
                .extracting(Trip::getName)
                .containsExactly("Paris");
    }

    @Test
    void existsByNameAndTripCreatorEmail_shouldReturnTrueOnlyForExistingTrip() {
        User creator = persistUser("creator@example.com");
        persistTrip("Paris", creator, TripStatus.PLANNED);

        entityManager.flush();
        entityManager.clear();

        assertThat(tripRepository.existsByNameAndTripCreator_Email("Paris", "creator@example.com")).isTrue();
        assertThat(tripRepository.existsByNameAndTripCreator_Email("Rome", "creator@example.com")).isFalse();
        assertThat(tripRepository.existsByNameAndTripCreator_Email("Paris", "other@example.com")).isFalse();
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

    private Trip persistTrip(String name, User creator, TripStatus status) {
        Trip trip = new Trip();
        trip.setName(name);
        trip.setDestination("Destination");
        trip.setStatus(status);
        trip.setTripCreator(creator);
        trip.setBudget(1000.0);
        trip.setStartDate(LocalDate.now());
        trip.setEndDate(LocalDate.now().plusDays(7));
        trip.setActualCost(0.0);
        entityManager.persist(trip);
        return trip;
    }
}
