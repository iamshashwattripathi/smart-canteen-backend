package com.smartcanteen.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id")
	private Order order;

	@Enumerated(EnumType.STRING)
	@Column(length = 10)
	private NotificationType type = NotificationType.PUSH;

	@Column(columnDefinition = "TEXT")
	private String message;

	@Column(length = 15)
	private String phone;

	@Enumerated(EnumType.STRING)
	@Column(length = 10)
	private NotificationStatus status = NotificationStatus.PENDING;

	// Whether the user has seen/dismissed this notification
	@Column(name = "is_read")
	private boolean isRead = false;

	@Column(name = "sent_at")
	private LocalDateTime sentAt = LocalDateTime.now();

	public enum NotificationType {
		SMS, EMAIL, PUSH
	}

	public enum NotificationStatus {
		SENT, FAILED, PENDING
	}
}
