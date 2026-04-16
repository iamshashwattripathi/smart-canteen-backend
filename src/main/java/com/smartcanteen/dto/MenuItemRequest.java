package com.smartcanteen.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuItemRequest {
	private Long stallId;
	private String name;
	private String category;
	private BigDecimal price;
	private boolean veg;
	private String description;
	private String imageUrl;
}
