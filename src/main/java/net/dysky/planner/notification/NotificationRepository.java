package net.dysky.planner.notification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findAllByStatus(NotificationStatus status);

    List<Notification> findAllByReceiver_EmailAndStatus(String receiverEmail, NotificationStatus status);
}
