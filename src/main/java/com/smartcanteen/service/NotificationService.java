package com.smartcanteen.service;

import com.smartcanteen.entity.Notification;
import com.smartcanteen.entity.Order;
import com.smartcanteen.entity.User;
import com.smartcanteen.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository  notificationRepository;
    private final SimpMessagingTemplate   messagingTemplate;

    // ── Create and broadcast a notification ─────────────────
    @Transactional
    public Notification notify(User user, Order order, String message) {
        Notification notification = Notification.builder()
                .user(user)
                .order(order)
                .type(Notification.NotificationType.PUSH)
                .message(message)
                .status(Notification.NotificationStatus.SENT)
                .build();

        Notification saved = notificationRepository.save(notification);

        // Push via WebSocket to /topic/notifications/{userId}
        try {
            messagingTemplate.convertAndSend(
                    "/topic/notifications/" + user.getId(),
                    Map.of(
                        "id",        saved.getId(),
                        "message",   saved.getMessage(),
                        "orderId",   order != null ? order.getId() : null,
                        "orderCode", order != null ? order.getOrderCode() : null,
                        "sentAt",    saved.getSentAt().toString()
                    )
            );
        } catch (Exception e) {
            log.warn("WebSocket push failed for user {}: {}", user.getId(), e.getMessage());
        }

        return saved;
    }

    // ── Get all notifications for a user ────────────────────
    public List<Notification> getForUser(Long userId) {
        return notificationRepository.findByUser_IdOrderBySentAtDesc(userId);
    }

    // ── Get unread notifications ─────────────────────────────
    public List<Notification> getUnread(Long userId) {
        return notificationRepository.findByUser_IdAndIsReadFalseOrderBySentAtDesc(userId);
    }

    // ── Unread count ─────────────────────────────────────────
    public long countUnread(Long userId) {
        return notificationRepository.countByUser_IdAndIsReadFalse(userId);
    }

    // ── Mark single notification as read ────────────────────
    @Transactional
    public void markRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    // ── Mark all as read for a user ──────────────────────────
    @Transactional
    public void markAllRead(Long userId) {
        List<Notification> unread =
                notificationRepository.findByUser_IdAndIsReadFalseOrderBySentAtDesc(userId);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    // ── Broadcast order status change via WebSocket ──────────
    // Frontend subscribes to /topic/order/{orderCode}
    public void broadcastOrderStatus(String orderCode, String status, String pickupTime) {
        try {
            messagingTemplate.convertAndSend(
                    "/topic/order/" + orderCode,
                    Map.of(
                        "orderCode",  orderCode,
                        "status",     status,
                        "pickupTime", pickupTime != null ? pickupTime : ""
                    )
            );
        } catch (Exception e) {
            log.warn("WebSocket order broadcast failed for {}: {}", orderCode, e.getMessage());
        }
    }
}
