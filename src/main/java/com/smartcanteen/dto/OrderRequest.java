package com.smartcanteen.dto;

import lombok.*;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class OrderRequest {
    private List<CartItem> items;
    private String paymentMethod;   // CASH | UPI | CARD | WALLET

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class CartItem {
        private Long menuItemId;
        private int  quantity;
    }
}
