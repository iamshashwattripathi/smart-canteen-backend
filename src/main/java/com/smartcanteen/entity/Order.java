package com.smartcanteen.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Order {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "order_code", nullable = false, unique = true, length = 20)
	private String orderCode; // e.g. ORD172688

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "stall_id")
	private Stall stall; // assigned by PriorityQueue logic

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private OrderStatus status = OrderStatus.PENDING;

	@Enumerated(EnumType.STRING)
	@Column(name = "payment_method", length = 20)
	private PaymentMethod paymentMethod = PaymentMethod.CASH;

	@Enumerated(EnumType.STRING)
	@Column(name = "payment_status", length = 20)
	private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

	@Column(precision = 10, scale = 2)
	private BigDecimal subtotal = BigDecimal.ZERO;

	@Column(name = "gst_amount", precision = 10, scale = 2)
	private BigDecimal gstAmount = BigDecimal.ZERO;

	@Column(name = "total_amount", precision = 10, scale = 2)
	private BigDecimal totalAmount = BigDecimal.ZERO;

	@Column(name = "pickup_time")
	private LocalDateTime pickupTime;

	@Column(name = "created_at")
	private LocalDateTime createdAt = LocalDateTime.now();

	@Column(name = "updated_at")
	private LocalDateTime updatedAt = LocalDateTime.now();

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<OrderItem> items = new ArrayList<>();

	@PreUpdate
	public void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

	// ── Enums ────────────────────────────────────────────────
	public enum OrderStatus {
		PENDING, PREPARING, READY, COMPLETED, CANCELLED
	}

	public enum PaymentMethod {
		CASH, UPI, CARD, WALLET
	}

	public enum PaymentStatus {
		PAID, UNPAID, REFUNDED
	}
}
