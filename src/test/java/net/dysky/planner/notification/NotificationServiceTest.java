package net.dysky.planner.notification;

import net.dysky.planner.user.User;
import net.dysky.planner.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private KafkaTemplate<String, NotificationDTO> kafkaTemplate;

    @Mock
    private NotificationRepository repository;

    @Mock
    private UserService userService;

    @InjectMocks
    private NotificationService notificationService;

    private User sender;
    private User receiver;
    private UUID receiverId;
    private UUID senderId;

    @BeforeEach
    void setUp() {
        receiverId = UUID.randomUUID();
        senderId = UUID.randomUUID();

        sender = new User();
        sender.setId(senderId);
        sender.setEmail("sender@planner.net");

        receiver = new User();
        receiver.setId(receiverId);
        receiver.setEmail("receiver@planner.net");
    }

    @Test
    void findById_shouldReturnNotification_whenNotificationExists() {
        UUID notificationId = UUID.randomUUID();
        Notification notification = new Notification();
        notification.setId(notificationId);

        when(repository.findById(notificationId)).thenReturn(Optional.of(notification));

        Notification result = notificationService.findById(notificationId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(notificationId);
    }

    @Test
    void findById_shouldThrowException_whenNotificationDoesNotExist() {
        UUID notificationId = UUID.randomUUID();
        when(repository.findById(notificationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.findById(notificationId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("This notification is not found");
    }

    @Test
    void sendNotification_shouldPublishEventToKafka() {
        notificationService.sendNotification("Test Title", "Test Message", receiverId);

        ArgumentCaptor<NotificationDTO> dtoCaptor = ArgumentCaptor.forClass(NotificationDTO.class);

        verify(kafkaTemplate).send(
                eq("planner-notification"),
                eq(receiverId.toString()),
                dtoCaptor.capture()
        );

        NotificationDTO captured = dtoCaptor.getValue();
        assertThat(captured.title()).isEqualTo("Test Title");
        assertThat(captured.message()).isEqualTo("Test Message");
        assertThat(captured.receiverId()).isEqualTo(receiverId);
    }

    @Test
    void createNotification_withSenderId_shouldSaveNotificationWithPendingStatus() {
        when(userService.getUserById(senderId)).thenReturn(sender);
        when(userService.getUserById(receiverId)).thenReturn(receiver);

        notificationService.createNotification("Trip Created", "You have a new trip", receiverId, senderId);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(repository).save(captor.capture());

        Notification saved = captor.getValue();
        assertThat(saved.getTitle()).isEqualTo("Trip Created");
        assertThat(saved.getMessage()).isEqualTo("You have a new trip");
        assertThat(saved.getStatus()).isEqualTo(NotificationStatus.PENDING);
        assertThat(saved.getSender()).isEqualTo(sender);
        assertThat(saved.getReceiver()).isEqualTo(receiver);
    }

    @Test
    void createNotification_withoutSenderId_shouldUseSystemUserAndSave() {
        User systemUser = new User();
        systemUser.setEmail("system@planner.net");

        when(userService.getUserByEmail("system@planner.net")).thenReturn(systemUser);
        when(userService.getUserById(receiverId)).thenReturn(receiver);

        notificationService.createNotification("System Alert", "Maintenance tonight", receiverId);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(repository).save(captor.capture());

        Notification saved = captor.getValue();
        assertThat(saved.getTitle()).isEqualTo("System Alert");
        assertThat(saved.getStatus()).isEqualTo(NotificationStatus.PENDING);
        assertThat(saved.getSender()).isEqualTo(systemUser);
        assertThat(saved.getReceiver()).isEqualTo(receiver);
    }

    @Test
    void sendToKafka_shouldPublishPendingNotificationsAndMarkThemSent() {
        Notification pendingNotification = new Notification();
        pendingNotification.setTitle("Upcoming Event");
        pendingNotification.setMessage("Starts in 1 hour");
        pendingNotification.setReceiver(receiver);
        pendingNotification.setStatus(NotificationStatus.PENDING);

        when(repository.findAllByStatus(NotificationStatus.PENDING))
                .thenReturn(List.of(pendingNotification));

        notificationService.sendToKafka();

        verify(kafkaTemplate).send(
                eq("planner-notification"),
                eq(receiverId.toString()),
                any(NotificationDTO.class)
        );

        assertThat(pendingNotification.getStatus()).isEqualTo(NotificationStatus.SENT);
        verify(repository).save(pendingNotification);
    }

    @Test
    void getAllUnreadNotifications_shouldReturnSentNotifications() {
        Notification notification = new Notification();
        notification.setStatus(NotificationStatus.SENT);

        when(repository.findAllByReceiver_EmailAndStatus("receiver@planner.net", NotificationStatus.SENT))
                .thenReturn(List.of(notification));

        List<Notification> result = notificationService.getAllUnreadNotifications("receiver@planner.net");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(NotificationStatus.SENT);
    }

    @Test
    void markNotificationAsRead_shouldUpdateStatusToRead_whenUserIsReceiver() {
        UUID notificationId = UUID.randomUUID();
        Notification notification = new Notification();
        notification.setId(notificationId);
        notification.setReceiver(receiver);
        notification.setStatus(NotificationStatus.SENT);

        when(repository.findById(notificationId)).thenReturn(Optional.of(notification));

        notificationService.markNotificationAsRead(notificationId, "receiver@planner.net");

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.READ);
        verify(repository).save(notification);
    }

    @Test
    void markNotificationAsRead_shouldThrowException_whenUserIsNotReceiver() {
        UUID notificationId = UUID.randomUUID();
        Notification notification = new Notification();
        notification.setId(notificationId);
        notification.setReceiver(receiver);
        notification.setStatus(NotificationStatus.SENT);

        when(repository.findById(notificationId)).thenReturn(Optional.of(notification));

        assertThatThrownBy(() -> notificationService.markNotificationAsRead(notificationId, "intruder@planner.net"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("You are not the receiver of this notification");

        verify(repository, never()).save(any());
    }

    @Test
    void markNotificationAsRead_shouldThrowException_whenAlreadyRead() {
        UUID notificationId = UUID.randomUUID();
        Notification notification = new Notification();
        notification.setId(notificationId);
        notification.setReceiver(receiver);
        notification.setStatus(NotificationStatus.READ);

        when(repository.findById(notificationId)).thenReturn(Optional.of(notification));

        assertThatThrownBy(() -> notificationService.markNotificationAsRead(notificationId, "receiver@planner.net"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("This notification is already read");

        verify(repository, never()).save(any());
    }

    @Test
    void markAllAsRead_shouldMarkAllSentNotificationsAsRead() {
        Notification first = new Notification();
        first.setStatus(NotificationStatus.SENT);

        Notification second = new Notification();
        second.setStatus(NotificationStatus.SENT);

        when(repository.findAllByReceiver_EmailAndStatus("receiver@planner.net", NotificationStatus.SENT))
                .thenReturn(List.of(first, second));

        notificationService.markAllAsRead("receiver@planner.net");

        assertThat(first.getStatus()).isEqualTo(NotificationStatus.READ);
        assertThat(second.getStatus()).isEqualTo(NotificationStatus.READ);

        verify(repository, times(2)).save(any(Notification.class));
    }
}