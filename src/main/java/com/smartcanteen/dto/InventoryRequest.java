package com.smartcanteen.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryRequest {
	private String materialName;
	private int stockQuantity;
	private String unit;
	private int lowStockThreshold;
}
