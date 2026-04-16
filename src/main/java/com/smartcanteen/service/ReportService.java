package com.smartcanteen.service;

import com.smartcanteen.entity.Order.PaymentStatus;
import com.smartcanteen.repository.OrderItemRepository;
import com.smartcanteen.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReportService {

	private final OrderRepository orderRepository;
	private final OrderItemRepository orderItemRepository;

	/** Full dashboard summary: total orders, revenue, top item, low stock count */
	public Map<String, Object> getDashboardSummary() {
		Map<String, Object> summary = new LinkedHashMap<>();

		long totalOrders = orderRepository.count();
		BigDecimal revenue = orderRepository.totalRevenue();
		long paidOrders = orderRepository.countByPaymentStatus(PaymentStatus.PAID);

		// Top selling item
		List<Object[]> topItems = orderItemRepository.topSellingItems();
		String topItem = topItems.isEmpty() ? "N/A" : (String) topItems.get(0)[0];

		summary.put("totalOrders", totalOrders);
		summary.put("totalRevenue", revenue);
		summary.put("paidOrders", paidOrders);
		summary.put("topSellingItem", topItem);

		return summary;
	}

	/** Sales trend for last 7 days — used by admin chart */
	public List<Map<String, Object>> getSalesTrend() {
		LocalDateTime since = LocalDateTime.now().minusDays(7);
		List<Object[]> rows = orderRepository.salesTrend(since);
		List<Map<String, Object>> result = new ArrayList<>();

		for (Object[] row : rows) {
			Map<String, Object> point = new LinkedHashMap<>();
			point.put("date", row[0].toString());
			point.put("revenue", row[1]);
			result.add(point);
		}
		return result;
	}

	/** Top selling items list for admin page */
	public List<Map<String, Object>> getTopSellingItems() {
		List<Object[]> rows = orderItemRepository.topSellingItems();
		List<Map<String, Object>> result = new ArrayList<>();

		for (Object[] row : rows) {
			Map<String, Object> item = new LinkedHashMap<>();
			item.put("name", row[0]);
			item.put("totalSold", row[1]);
			result.add(item);
		}
		return result;
	}
}
