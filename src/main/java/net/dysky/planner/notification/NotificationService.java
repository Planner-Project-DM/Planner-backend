package net.dysky.planner.notification;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.dysky.planner.user.User;
import net.dysky.planner.user.UserService;
import net.dysky.planner.usersettings.UserSettings;
import net.dysky.planner.usersettings.UserSettingsService;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final KafkaTemplate<String, NotificationDTO> kafkaTemplate;

    private final NotificationRepository repository;

    private final UserService userService;

    private final UserSettingsService userSettingsService;

    public Notification findById(UUID notificationId) {
        return repository.findById(notificationId).orElseThrow(
                () -> new IllegalArgumentException("This notification is not found"));
    }

    public void sendNotification(String title, String message, UUID receiverId) {
        NotificationDTO dto = new NotificationDTO(title, message, receiverId);

        kafkaTemplate.send("planner-notification", receiverId.toString(), dto);
    }

    @Transactional
    public void createNotification(String title, String message, UUID receiverId, UUID senderId) {
        User sender = userService.getUserById(senderId);
        User receiver = userService.getUserById(receiverId);

        Notification notification = new Notification();

        notification.setSender(sender);
        notification.setReceiver(receiver);

        notification.setTitle(title);
        notification.setMessage(message);

        notification.setStatus(NotificationStatus.PENDING);

        repository.save(notification);
    }

    @Transactional
    public void createNotification(String title, String message, UUID receiverId) {
        User sender = userService.getUserByEmail("system@planner.net");
        User receiver = userService.getUserById(receiverId);

        Notification notification = new Notification();

        notification.setSender(sender);
        notification.setReceiver(receiver);

        notification.setTitle(title);
        notification.setMessage(message);

        notification.setStatus(NotificationStatus.PENDING);

        repository.save(notification);
    }

    @Scheduled(fixedDelay = 20000)
    public void sendToKafka() {
        List<Notification> notifications = repository.findAllByStatus(NotificationStatus.PENDING);

        for(Notification notification : notifications) {
            sendNotification(notification.getTitle(), notification.getMessage(), notification.getReceiver().getId());
            notification.setStatus(NotificationStatus.SENT);
            repository.save(notification);
        }
    }

    public List<Notification> getAllUnreadNotifications(String email) {
        return repository.findAllByReceiver_EmailAndStatus(email, NotificationStatus.SENT);
    }

    public void markNotificationAsRead(UUID id, String email) {
        Notification notification = findById(id);

        if (!notification.getReceiver().getEmail().equals(email)) {
            throw new IllegalArgumentException("You are not the receiver of this notification");
        }

        if (notification.getStatus() == NotificationStatus.READ) {
            throw new IllegalArgumentException("This notification is already read");
        }

        notification.setStatus(NotificationStatus.READ);
        repository.save(notification);
    }

    public void markAllAsRead(String email) {
        List<Notification> notifications = repository.findAllByReceiver_EmailAndStatus(email, NotificationStatus.SENT);

        for(Notification notification : notifications) {
            notification.setStatus(NotificationStatus.READ);
            repository.save(notification);
        }
    }

    public boolean shouldNotify(UUID userId, NotificationType type) {
        UserSettings s = userSettingsService.findByUserId(userId);

        if (!s.isNotificationEnabled()) return false;

        return switch (type) {
            case FRIENDSHIP_REQUEST -> s.isNotifyFriendshipRequest();
            case FRIENDSHIP_REMOVED -> s.isNotifyFriendshipRemoved();
            case SCHEDULE_ITEM_ADDED -> s.isNotifyScheduleItemAdded();
            case SCHEDULE_ITEM_UPDATED -> s.isNotifyScheduleItemUpdated();
            case SCHEDULE_ITEM_DELETED -> s.isNotifyScheduleItemDeleted();
            case GROUP_MEMBER_ADDED -> s.isNotifyGroupMemberAdded();
            case GROUP_MEMBER_REMOVED  -> s.isNotifyGroupMemberRemoved();
            case FUND_ITEM_COST_UPDATED  -> s.isNotifyFundItemCostUpdated();
        };
    }

}
