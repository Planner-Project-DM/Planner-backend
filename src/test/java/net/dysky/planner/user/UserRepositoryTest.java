package net.dysky.planner.user;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import net.dysky.planner.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class UserRepositoryTest  extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void findByEmail_shouldReturnUserWhenEmailExists() {
        persistUser("user@example.com");

        entityManager.flush();
        entityManager.clear();

        User user = userRepository.findByEmail("user@example.com").orElseThrow();

        assertThat(user.getEmail()).isEqualTo("user@example.com");
        assertThat(user.getFirstName()).isEqualTo("Test");
        assertThat(user.getRole()).isEqualTo(UserRole.USER);
        assertThat(user.getIsActive()).isTrue();
    }

    @Test
    void findByEmail_shouldReturnEmptyWhenEmailDoesNotExist() {
        persistUser("user@example.com");

        entityManager.flush();
        entityManager.clear();

        assertThat(userRepository.findByEmail("missing@example.com")).isEmpty();
    }

    @Test
    void existsByEmail_shouldReturnTrueOnlyForPersistedEmail() {
        persistUser("user@example.com");

        entityManager.flush();
        entityManager.clear();

        assertThat(userRepository.existsByEmail("user@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("missing@example.com")).isFalse();
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
}
