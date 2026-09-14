package net.dysky.planner.notification;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface NotificationRepository extends JpaRepository<Notification, UUID> {

    @EntityGraph(attributePaths = {"sender", "receiver"})
    List<Notification> findAllByStatus(NotificationStatus status);

    @EntityGraph(attributePaths = {"sender", "receiver"})
    List<Notification> findAllByReceiver_EmailAndStatus(String receiverEmail, NotificationStatus status);
}
