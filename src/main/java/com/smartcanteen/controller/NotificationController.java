package com.smartcanteen.controller;

import com.smartcanteen.entity.Notification;
import com.smartcanteen.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Notifications API
 *
 * GET    /api/notifications/{userId}         — All notifications for user
 * GET    /api/notifications/{userId}/unread  — Only unread notifications
 * GET    /api/notifications/{userId}/count   — Unread badge count
 * PATCH  /api/notifications/{id}/read        — Mark one as read
 * PATCH  /api/notifications/{userId}/read-all — Mark all as read
 *
 * Real-time pushes arrive via WebSocket at /topic/notifications/{userId}
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/{userId}")
    public ResponseEntity<List<Notification>> all(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getForUser(userId));
    }

    @GetMapping("/{userId}/unread")
    public ResponseEntity<List<Notification>> unread(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getUnread(userId));
    }

    @GetMapping("/{userId}/count")
    public ResponseEntity<Map<String, Long>> count(@PathVariable Long userId) {
        return ResponseEntity.ok(Map.of("unreadCount", notificationService.countUnread(userId)));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable Long id) {
        notificationService.markRead(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{userId}/read-all")
    public ResponseEntity<Void> markAllRead(@PathVariable Long userId) {
        notificationService.markAllRead(userId);
        return ResponseEntity.noContent().build();
    }
}
