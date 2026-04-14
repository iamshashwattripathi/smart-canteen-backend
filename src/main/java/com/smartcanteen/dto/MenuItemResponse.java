package com.smartcanteen.dto;

import lombok.*;
import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class MenuItemResponse {
    private Long       id;
    private String     stallName;
    private String     name;
    private String     category;
    private BigDecimal price;
    private boolean    veg;
    private boolean    available;
    private String     description;
    private String     imageUrl;
}
