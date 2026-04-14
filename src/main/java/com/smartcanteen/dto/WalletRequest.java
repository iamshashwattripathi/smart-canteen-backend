package com.smartcanteen.dto;

import lombok.*;
import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor
public class WalletRequest {
    private BigDecimal amount;
}
