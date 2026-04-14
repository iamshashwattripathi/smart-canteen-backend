package com.smartcanteen.repository;

import com.smartcanteen.entity.Order;
import com.smartcanteen.entity.Order.OrderStatus;
import com.smartcanteen.entity.Order.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderCode(String orderCode);

    List<Order> findByUser_Id(Long userId);

    List<Order> findByStatus(OrderStatus status);

    long countByPaymentStatus(PaymentStatus status);

    // Total revenue from paid orders
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.paymentStatus = 'PAID'")
    java.math.BigDecimal totalRevenue();

    // Revenue per day for the last 7 days (for sales trend chart)
    @Query("""
        SELECT FUNCTION('DATE', o.createdAt) AS day,
               SUM(o.totalAmount)            AS revenue
        FROM Order o
        WHERE o.paymentStatus = 'PAID'
          AND o.createdAt >= :since
        GROUP BY FUNCTION('DATE', o.createdAt)
        ORDER BY FUNCTION('DATE', o.createdAt)
        """)
    List<Object[]> salesTrend(@Param("since") LocalDateTime since);
}
