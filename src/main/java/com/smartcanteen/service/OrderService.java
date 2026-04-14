package com.smartcanteen.service;

import com.smartcanteen.entity.*;
import com.smartcanteen.entity.Order.*;
import com.smartcanteen.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository     orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final MenuItemRepository  menuItemRepository;
    private final UserRepository      userRepository;
    private final StallRepository     stallRepository;
    private final TokenRepository     tokenRepository;
    private final QueueService        queueService;
    private final NotificationService notificationService;

    @Value("${app.gst.rate:5}")
    private int gstRate;

    // ── Place a new order ────────────────────────────────────
    @Transactional
    public Order placeOrder(Long userId, List<Map<String, Object>> cartItems,
                            String paymentMethod) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 1. Assign least-busy stall via PriorityQueue logic
        Stall assignedStall = queueService.assignLeastBusyStall();

        // 2. Build order shell
        Order order = Order.builder()
                .orderCode(generateOrderCode())
                .user(user)
                .stall(assignedStall)
                .status(OrderStatus.PENDING)
                .paymentMethod(PaymentMethod.valueOf(paymentMethod.toUpperCase()))
                .paymentStatus(PaymentStatus.UNPAID)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // 3. Build order items and calculate subtotal
        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (Map<String, Object> cartItem : cartItems) {
            Long menuItemId = Long.parseLong(cartItem.get("menuItemId").toString());
            int  quantity   = Integer.parseInt(cartItem.get("quantity").toString());

            MenuItem menuItem = menuItemRepository.findById(menuItemId)
                    .orElseThrow(() -> new RuntimeException("Menu item not found: " + menuItemId));

            BigDecimal itemSubtotal = menuItem.getPrice()
                    .multiply(BigDecimal.valueOf(quantity));

            OrderItem oi = OrderItem.builder()
                    .order(order)
                    .menuItem(menuItem)
                    .quantity(quantity)
                    .unitPrice(menuItem.getPrice())
                    .subtotal(itemSubtotal)
                    .build();

            orderItems.add(oi);
            subtotal = subtotal.add(itemSubtotal);
        }

        // 4. Calculate GST and total
        BigDecimal gstAmount = subtotal
                .multiply(BigDecimal.valueOf(gstRate))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = subtotal.add(gstAmount);

        order.setSubtotal(subtotal);
        order.setGstAmount(gstAmount);
        order.setTotalAmount(totalAmount);
        order.setItems(orderItems);

        // 5. Estimate pickup time based on queue depth
        LocalDateTime pickupTime = queueService.estimatePickupTime(assignedStall);
        order.setPickupTime(pickupTime);

        // 6. Save order
        Order savedOrder = orderRepository.save(order);

        // 7. Issue a queue token for the assigned stall
        int nextToken = tokenRepository
                .findMaxTokenToday()
                .map(t -> t + 1)
                .orElse(1);

        Token token = Token.builder()
                .tokenNumber(nextToken)
                .order(savedOrder)
                .stall(assignedStall)
                .status(Token.TokenStatus.WAITING)
                .issuedAt(LocalDateTime.now())
                .build();
        tokenRepository.save(token);

        // 8. Deduct wallet if payment method is WALLET
        if (PaymentMethod.WALLET.name().equalsIgnoreCase(paymentMethod)) {
            if (user.getWalletBalance().compareTo(totalAmount) < 0) {
                throw new RuntimeException("Insufficient wallet balance");
            }
            user.setWalletBalance(user.getWalletBalance().subtract(totalAmount));
            userRepository.save(user);
            savedOrder.setPaymentStatus(PaymentStatus.PAID);
            orderRepository.save(savedOrder);
        }

        // 9. Send "order placed" notification
        String eta = savedOrder.getPickupTime() != null
                ? savedOrder.getPickupTime().toLocalTime().toString()
                : "soon";
        notificationService.notify(user, savedOrder,
                "Your order " + savedOrder.getOrderCode()
                + " has been placed! Token #" + nextToken
                + " at " + assignedStall.getName()
                + ". Estimated pickup: " + eta);

        return savedOrder;
    }

    // ── Update order status (admin / stall) ─────────────────
    @Transactional
    public Order updateStatus(Long orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        OrderStatus status = OrderStatus.valueOf(newStatus.toUpperCase());
        order.setStatus(status);

        // Release stall slot when order is done
        if (status == OrderStatus.COMPLETED || status == OrderStatus.CANCELLED) {
            queueService.releaseStallSlot(order.getStall());
            if (status == OrderStatus.COMPLETED) {
                order.setPaymentStatus(PaymentStatus.PAID);
            }
        }

        Order saved = orderRepository.save(order);

        // Fire status-change notifications + WebSocket broadcast
        if (order.getUser() != null) {
            String msg = switch (status) {
                case PREPARING  -> "Your order " + order.getOrderCode() + " is now being prepared!";
                case READY      -> "Your order " + order.getOrderCode() + " is READY for pickup!";
                case COMPLETED  -> "Thank you! Order " + order.getOrderCode() + " completed. Enjoy your meal!";
                case CANCELLED  -> "Your order " + order.getOrderCode() + " has been cancelled.";
                default         -> null;
            };
            if (msg != null) {
                notificationService.notify(order.getUser(), order, msg);
            }
        }

        // Broadcast to /topic/order/{orderCode} for live order tracking page
        notificationService.broadcastOrderStatus(
                order.getOrderCode(),
                status.name(),
                saved.getPickupTime() != null ? saved.getPickupTime().toString() : null
        );

        return saved;
    }

    // ── Get all orders for a user ────────────────────────────
    public List<Order> getOrdersByUser(Long userId) {
        return orderRepository.findByUser_Id(userId);
    }

    // ── Get order by code ────────────────────────────────────
    public Order getByCode(String orderCode) {
        return orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderCode));
    }

    // ── Generate unique order code ───────────────────────────
    private String generateOrderCode() {
        return "ORD" + (10000 + new Random().nextInt(90000));
    }
}
