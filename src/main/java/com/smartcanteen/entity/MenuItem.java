package com.smartcanteen.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "menu_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class MenuItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "stall_id")
	private Stall stall;

	@Column(nullable = false, length = 150)
	private String name;

	@Column(length = 100)
	private String category;

	@Column(nullable = false, precision = 8, scale = 2)
	private BigDecimal price;

	@Column(name = "is_veg")
	private boolean veg = true;

	@Column(name = "is_available")
	private boolean available = true;

	@Column(name = "image_url", length = 300)
	private String imageUrl;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(name = "created_at")
	private LocalDateTime createdAt = LocalDateTime.now();
}
