package com.smartcanteen.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "inventory")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Inventory {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "material_name", nullable = false, length = 150)
    private String materialName;

    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity = 0;

    @Column(length = 30)
    private String unit = "units";

    @Column(name = "low_stock_threshold")
    private int lowStockThreshold = 5;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated = LocalDateTime.now();

    @PreUpdate
    public void onUpdate() { this.lastUpdated = LocalDateTime.now(); }

    /** Computed stock status — not stored in DB */
    @Transient
    public String getStatus() {
        if (stockQuantity <= lowStockThreshold)           return "LOW_STOCK";
        if (stockQuantity <= lowStockThreshold * 2)       return "MEDIUM";
        return "GOOD";
    }
}
