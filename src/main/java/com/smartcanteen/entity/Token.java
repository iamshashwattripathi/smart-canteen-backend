package com.smartcanteen.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Token {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "token_number", nullable = false)
	private int tokenNumber;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id")
	private Order order;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "stall_id")
	private Stall stall;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private TokenStatus status = TokenStatus.WAITING;

	@Column(name = "issued_at")
	private LocalDateTime issuedAt = LocalDateTime.now();

	public enum TokenStatus {
		WAITING, SERVING, DONE
	}
}
