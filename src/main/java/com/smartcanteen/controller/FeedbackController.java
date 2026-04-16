package com.smartcanteen.controller;

import com.smartcanteen.entity.Feedback;
import com.smartcanteen.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Feedback & Support API
 *
 * POST /api/feedback — Submit feedback (authenticated customer) GET
 * /api/feedback/my/{userId} — Get my feedback history GET /api/feedback/all —
 * All feedback [ADMIN]
 */
@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {

	private final FeedbackService feedbackService;

	// Submit feedback
	// Body: { userId, rating (1-5), comment, category?, orderId? }
	@PostMapping
	public ResponseEntity<Feedback> submit(@RequestBody Map<String, Object> payload) {
		Long userId = Long.parseLong(payload.get("userId").toString());
		Feedback feedback = feedbackService.submitFeedback(userId, payload);
		return ResponseEntity.ok(feedback);
	}

	// Get feedback submitted by a specific user
	@GetMapping("/my/{userId}")
	public ResponseEntity<List<Feedback>> myFeedback(@PathVariable Long userId) {
		return ResponseEntity.ok(feedbackService.getFeedbackByUser(userId));
	}

	// Get all feedback (admin use)
	@GetMapping("/all")
	public ResponseEntity<List<Feedback>> all() {
		return ResponseEntity.ok(feedbackService.getAllFeedback());
	}
}
