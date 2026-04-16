package com.smartcanteen.controller;

import com.smartcanteen.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ReportController {

	private final ReportService reportService;

	/**
	 * GET /api/reports/dashboard Returns: totalOrders, totalRevenue,
	 * topSellingItem, paidOrders Powers the Admin Dashboard summary cards.
	 */
	@GetMapping("/dashboard")
	public ResponseEntity<?> dashboard() {
		return ResponseEntity.ok(reportService.getDashboardSummary());
	}

	/**
	 * GET /api/reports/sales-trend Returns daily revenue for last 7 days — powers
	 * the line chart.
	 */
	@GetMapping("/sales-trend")
	public ResponseEntity<?> salesTrend() {
		return ResponseEntity.ok(reportService.getSalesTrend());
	}

	/**
	 * GET /api/reports/top-items Returns top selling items list — powers the item
	 * performance section.
	 */
	@GetMapping("/top-items")
	public ResponseEntity<?> topItems() {
		return ResponseEntity.ok(reportService.getTopSellingItems());
	}
}
