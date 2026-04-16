package com.smartcanteen.service;

import com.smartcanteen.entity.Feedback;
import com.smartcanteen.entity.Order;
import com.smartcanteen.entity.User;
import com.smartcanteen.repository.FeedbackRepository;
import com.smartcanteen.repository.OrderRepository;
import com.smartcanteen.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FeedbackService {

	private final FeedbackRepository feedbackRepository;
	private final UserRepository userRepository;
	private final OrderRepository orderRepository;

	@Transactional
	public Feedback submitFeedback(Long userId, Map<String, Object> payload) {
		User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

		int rating = Integer.parseInt(payload.get("rating").toString());
		if (rating < 1 || rating > 5) {
			throw new RuntimeException("Rating must be between 1 and 5");
		}

		Feedback.FeedbackCategory category = Feedback.FeedbackCategory.GENERAL;
		if (payload.containsKey("category")) {
			try {
				category = Feedback.FeedbackCategory.valueOf(payload.get("category").toString().toUpperCase());
			} catch (IllegalArgumentException ignored) {
			}
		}

		Order order = null;
		if (payload.containsKey("orderId") && payload.get("orderId") != null) {
			Long orderId = Long.parseLong(payload.get("orderId").toString());
			order = orderRepository.findById(orderId).orElse(null);
		}

		Feedback feedback = Feedback.builder().user(user).order(order).rating(rating)
				.comment(payload.getOrDefault("comment", "").toString()).category(category).build();

		return feedbackRepository.save(feedback);
	}

	public List<Feedback> getFeedbackByUser(Long userId) {
		return feedbackRepository.findByUser_IdOrderByCreatedAtDesc(userId);
	}

	public List<Feedback> getAllFeedback() {
		return feedbackRepository.findAllByOrderByCreatedAtDesc();
	}
}
