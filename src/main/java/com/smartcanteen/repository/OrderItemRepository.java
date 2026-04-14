package com.smartcanteen.repository;

import com.smartcanteen.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * Returns [itemName, totalQuantitySold] ordered by most sold.
     * Used by the admin dashboard and AI suggestion feature.
     */
    @Query("""
        SELECT oi.menuItem.name, SUM(oi.quantity)
        FROM OrderItem oi
        JOIN oi.order o
        WHERE o.paymentStatus = 'PAID'
        GROUP BY oi.menuItem.name
        ORDER BY SUM(oi.quantity) DESC
        """)
    List<Object[]> topSellingItems();
}
