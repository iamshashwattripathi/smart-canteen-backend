// ════════════════════════════════════════════════════════════
//  ENTITY CLASSES  (one file for easy reading — split into
//  separate files when you import into your IDE)
// ════════════════════════════════════════════════════════════

// ── File: entity/User.java ───────────────────────────────────
package com.smartcanteen.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "password" })
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(nullable = false, unique = true, length = 150)
	private String email;

	@Column(nullable = false)
	private String password;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private Role role = Role.CUSTOMER;

	@Column(length = 15)
	private String phone;

	@Column(name = "wallet_balance", precision = 10, scale = 2)
	private BigDecimal walletBalance = BigDecimal.ZERO;

	@Column(name = "created_at")
	private LocalDateTime createdAt = LocalDateTime.now();

	public enum Role {
		CUSTOMER, ADMIN, STAFF
	}
}
