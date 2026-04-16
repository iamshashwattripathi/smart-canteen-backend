package com.smartcanteen.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
public class WalletResponse {
	private BigDecimal balance;
	private String message;
}
