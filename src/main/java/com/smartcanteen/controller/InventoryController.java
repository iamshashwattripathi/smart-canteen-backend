package com.smartcanteen.controller;

import com.smartcanteen.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class InventoryController {

	private final InventoryService inventoryService;

	/** GET /api/inventory — full stock list */
	@GetMapping
	public ResponseEntity<?> getAll() {
		return ResponseEntity.ok(inventoryService.getAll());
	}

	/** GET /api/inventory/low — only low-stock items (for alert banner) */
	@GetMapping("/low")
	public ResponseEntity<?> getLowStock() {
		return ResponseEntity.ok(inventoryService.getLowStockItems());
	}

	/**
	 * POST /api/inventory Body: { "materialName":"Bread", "stockQuantity":20,
	 * "unit":"pieces", "lowStockThreshold":5 }
	 */
	@PostMapping
	public ResponseEntity<?> saveOrUpdate(@RequestBody Map<String, Object> body) {
		try {
			var item = inventoryService.saveOrUpdate(body.get("materialName").toString(),
					Integer.parseInt(body.get("stockQuantity").toString()),
					body.getOrDefault("unit", "units").toString(),
					body.containsKey("lowStockThreshold") ? Integer.parseInt(body.get("lowStockThreshold").toString())
							: 5);
			return ResponseEntity.ok(item);
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}

	/** DELETE /api/inventory/{id} */
	@DeleteMapping("/{id}")
	public ResponseEntity<?> delete(@PathVariable Long id) {
		inventoryService.delete(id);
		return ResponseEntity.ok(Map.of("message", "Deleted"));
	}
}
