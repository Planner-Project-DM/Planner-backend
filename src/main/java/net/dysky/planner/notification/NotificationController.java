package net.dysky.planner.notification;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import net.dysky.planner.auth.JwtService;
import net.dysky.planner.response.ResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
class NotificationController {

    private final NotificationService notificationService;

    private final JwtService jwtService;

    @GetMapping("/unread")
    public ResponseEntity<ResponseDTO> getAllUnreadNotifications(HttpServletRequest request)  {
        String email = jwtService.extractEmail(request);

        List<Notification> notifications = notificationService.getAllUnreadNotifications(email);

        return ResponseEntity.ok(new ResponseDTO(
                java.time.LocalDateTime.now(),
                200,
                "Unread notifications retrieved successfully",
                "/api/notifications/unread",
                notifications
        ));
    }

    @PostMapping("/{id}/markAsRead")
    public ResponseEntity<ResponseDTO> markNotificationAsRead(@PathVariable("id") UUID id, HttpServletRequest request) {
        String email = jwtService.extractEmail(request);

        notificationService.markNotificationAsRead(id, email);

        return ResponseEntity.ok(new ResponseDTO(
                java.time.LocalDateTime.now(),
                200,
                "Notification marked as read successfully",
                "/api/notifications/" + id + "/markAsRead",
                null
        ));
    }

    @PostMapping("/markAllAsRead")
    public ResponseEntity<ResponseDTO> markAllAsRead(HttpServletRequest request) {
        String email = jwtService.extractEmail(request);

        notificationService.markAllAsRead(email);

        return ResponseEntity.ok(new ResponseDTO(
                java.time.LocalDateTime.now(),
                200,
                "All notifications marked as read successfully",
                "/api/notifications/markAllAsRead",
                null
        ));
    }

}
