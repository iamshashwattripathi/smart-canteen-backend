package com.smartcanteen.controller;

import com.smartcanteen.entity.Order;
import com.smartcanteen.service.OrderService;
import com.smartcanteen.service.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class OrderController {

	private final OrderService orderService;
	private final QueueService queueService;

	// POST /api/orders/place
	@PostMapping("/place")
	public ResponseEntity<?> placeOrder(@RequestBody Map<String, Object> body) {
		try {
			Long userId = Long.parseLong(body.get("userId").toString());
			String paymentMethod = body.get("paymentMethod").toString();
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> cartItems = (List<Map<String, Object>>) body.get("cartItems");

			Order order = orderService.placeOrder(userId, cartItems, paymentMethod);

			return ResponseEntity.ok(Map.of("message", "Order placed successfully", "orderId", order.getId(),
					"orderCode", order.getOrderCode(), "stallAssigned", order.getStall().getName(), "pickupTime",
					order.getPickupTime() != null ? order.getPickupTime().toString() : "N/A", "totalAmount",
					order.getTotalAmount(), "status", order.getStatus().name()));
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}

	// GET /api/orders/user/{userId}
	@GetMapping("/user/{userId}")
	public ResponseEntity<?> getUserOrders(@PathVariable Long userId) {
		return ResponseEntity.ok(orderService.getOrdersByUser(userId));
	}

	// GET /api/orders/track/{orderCode}
	@GetMapping("/track/{orderCode}")
	public ResponseEntity<?> trackOrder(@PathVariable String orderCode) {
		try {
			Order order = orderService.getByCode(orderCode);
			return ResponseEntity.ok(Map.of("orderCode", order.getOrderCode(), "status", order.getStatus().name(),
					"stall", order.getStall().getName(), "pickupTime",
					order.getPickupTime() != null ? order.getPickupTime().toString() : "N/A", "totalAmount",
					order.getTotalAmount()));
		} catch (RuntimeException e) {
			return ResponseEntity.notFound().build();
		}
	}

	// PATCH /api/orders/{id}/status [ADMIN/STAFF]
	@PatchMapping("/{id}/status")
	public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
		try {
			Order updated = orderService.updateStatus(id, body.get("status"));
			return ResponseEntity.ok(Map.of("message", "Status updated", "orderId", updated.getId(), "status",
					updated.getStatus().name()));
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}

	// GET /api/orders/queue/snapshot [ADMIN]
	@GetMapping("/queue/snapshot")
	public ResponseEntity<?> queueSnapshot() {
		return ResponseEntity.ok(queueService.getQueueSnapshot());
	}
}
