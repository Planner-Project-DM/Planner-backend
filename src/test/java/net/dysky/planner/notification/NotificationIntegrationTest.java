package net.dysky.planner.notification;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import net.dysky.planner.AbstractIntegrationTest;
import net.dysky.planner.auth.JwtService;
import net.dysky.planner.auth.RegisterDTO;
import net.dysky.planner.user.User;
import net.dysky.planner.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
public class NotificationIntegrationTest extends AbstractIntegrationTest {

    private static final String TOPIC = "planner-notification";
    private static final String TEST_EMAIL = "john.doe@planner.net";

    @Autowired
    private KafkaTemplate<String, NotificationDTO> kafkaTemplate;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserService userService;

    @MockitoSpyBean
    private NotificationConsumer notificationConsumer;

    @MockitoBean
    private JwtService jwtService;

    private User testUser;
    private String testEmail;
    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();

        testEmail = "user." + UUID.randomUUID() + "@planner.net";

        when(jwtService.extractEmail(any(HttpServletRequest.class))).thenReturn(testEmail);

        RegisterDTO registerDTO = new RegisterDTO(
                "John",
                "Doe",
                testEmail,
                "123456789",
                "securePassword123"
        );
        testUser = userService.createUser(registerDTO);
    }

    @Test
    void shouldConsumeNotificationFromKafkaTopic() {
        NotificationDTO dto = new NotificationDTO(
                "Trip Invitation",
                "You have been invited to join the trip.",
                testUser.getId()
        );

        kafkaTemplate.send(TOPIC, testUser.getId().toString(), dto);

        await()
                .atMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(200))
                .untilAsserted(() -> {
                    verify(notificationConsumer).consume(dto);
                });
    }

    @Test
    void shouldSendPendingNotificationsToKafkaAndMarkAsSent() {
        Notification notification = new Notification();
        notification.setSender(testUser);
        notification.setReceiver(testUser);
        notification.setTitle("Reminder");
        notification.setMessage("Pack your passport.");
        notification.setStatus(NotificationStatus.PENDING);

        notificationRepository.saveAndFlush(notification);

        notificationService.sendToKafka();

        Notification updated = notificationRepository.findById(notification.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(NotificationStatus.SENT);

        await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    verify(notificationConsumer).consume(any(NotificationDTO.class));
                });
    }

    @Test
    void shouldReturnUnreadNotificationsViaApi() throws Exception {
        Notification notification = new Notification();
        notification.setSender(testUser);
        notification.setReceiver(testUser);
        notification.setTitle("Weather Alert");
        notification.setMessage("Rain expected.");
        notification.setStatus(NotificationStatus.SENT);

        notificationRepository.saveAndFlush(notification);

        mockMvc.perform(get("/api/notifications/unread")
                        .with(user(testEmail).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("Weather Alert"));
    }
}