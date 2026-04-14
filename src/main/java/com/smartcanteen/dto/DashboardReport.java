package com.smartcanteen.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data @Builder
public class DashboardReport {
    private long       totalOrders;
    private BigDecimal totalSales;
    private String     topSellingItem;
    private long       lowStockAlerts;
    private List<SalesTrendPoint> salesTrend;
    private List<TopItem>         topItems;

    @Data @AllArgsConstructor
    public static class SalesTrendPoint {
        private String     day;
        private BigDecimal revenue;
    }

    @Data @AllArgsConstructor
    public static class TopItem {
        private String itemName;
        private long   totalSold;
    }
}
