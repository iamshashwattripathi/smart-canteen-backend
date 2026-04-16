package com.smartcanteen.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "feedback")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Feedback {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id")
	private Order order;

	// 1 = very poor, 5 = excellent
	@Column(nullable = false)
	private int rating;

	@Column(columnDefinition = "TEXT")
	private String comment;

	// e.g. FOOD_QUALITY, SERVICE, APP_EXPERIENCE, OTHER
	@Enumerated(EnumType.STRING)
	@Column(length = 30)
	private FeedbackCategory category = FeedbackCategory.GENERAL;

	@Column(name = "created_at")
	private LocalDateTime createdAt = LocalDateTime.now();

	public enum FeedbackCategory {
		FOOD_QUALITY, SERVICE, APP_EXPERIENCE, CLEANLINESS, GENERAL
	}
}
