package com.smartcanteen.service;

import com.smartcanteen.repository.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * AI Food Suggestion Service
 *
 * Uses order history data to recommend the most popular item —
 * powering the "AI Food Suggestion" page in the frontend.
 * Based on frequency analysis (most ordered item = recommendation).
 */
@Service
@RequiredArgsConstructor
public class AISuggestionService {

    private final OrderItemRepository orderItemRepository;

    /**
     * Returns the top recommended food item based on order frequency.
     * If no orders yet, returns a default suggestion.
     */
    public Map<String, Object> getRecommendation() {
        List<Object[]> topItems = orderItemRepository.topSellingItems();

        if (topItems.isEmpty()) {
            return Map.of(
                    "recommended", "Veg Burger",
                    "reason",      "Most popular item (default)",
                    "totalOrders", 0
            );
        }

        Object[] top = topItems.get(0);
        String itemName   = (String) top[0];
        long   totalSold  = ((Number) top[1]).longValue();

        return Map.of(
                "recommended", itemName,
                "reason",      "Most ordered item with " + totalSold + " orders",
                "totalOrders", totalSold,
                "allTopItems", topItems.stream().limit(5).map(row ->
                        Map.of("name", row[0], "count", row[1])
                ).toList()
        );
    }
}
