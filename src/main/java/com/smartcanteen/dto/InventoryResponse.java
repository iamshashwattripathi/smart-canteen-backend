package com.smartcanteen.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryResponse {
	private Long id;
	private String materialName;
	private int stockQuantity;
	private String unit;
	private int lowStockThreshold;
	private String status; // GOOD | MEDIUM | LOW_STOCK
	private String lastUpdated;
}
