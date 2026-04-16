package com.smartcanteen.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class OrderResponse {
	private Long orderId;
	private String orderCode;
	private String stallName;
	private String status;
	private String paymentMethod;
	private String paymentStatus;
	private BigDecimal subtotal;
	private BigDecimal gstAmount;
	private BigDecimal totalAmount;
	private String pickupTime;
	private String createdAt;
	private Integer tokenNumber;
	private List<ItemLine> items;

	@Data
	@Builder
	public static class ItemLine {
		private String menuItemName;
		private int quantity;
		private BigDecimal unitPrice;
		private BigDecimal subtotal;
	}
}
