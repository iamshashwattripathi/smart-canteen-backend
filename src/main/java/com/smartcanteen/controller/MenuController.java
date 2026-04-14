package com.smartcanteen.controller;

import com.smartcanteen.entity.MenuItem;
import com.smartcanteen.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/menu")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    /**
     * GET /api/menu
     * Returns all available menu items grouped by category.
     * Powers the frontend index.html menu grid.
     */
    @GetMapping
    public ResponseEntity<?> getMenu() {
        return ResponseEntity.ok(menuService.getMenuGroupedByCategory());
    }

    /**
     * GET /api/menu/all
     * Returns flat list (for cart validation).
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAllItems() {
        return ResponseEntity.ok(menuService.getAllAvailable());
    }

    /**
     * GET /api/menu/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getItem(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(menuService.getById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * POST /api/menu  [ADMIN]
     * Adds a new menu item.
     */
    @PostMapping
    public ResponseEntity<?> addItem(@RequestBody MenuItem item) {
        return ResponseEntity.ok(menuService.save(item));
    }

    /**
     * PUT /api/menu/{id}  [ADMIN]
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateItem(@PathVariable Long id,
                                        @RequestBody MenuItem item) {
        item.setId(id);
        return ResponseEntity.ok(menuService.save(item));
    }

    /**
     * PATCH /api/menu/{id}/toggle  [ADMIN]
     * Toggles item availability on/off.
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<?> toggleItem(@PathVariable Long id) {
        return ResponseEntity.ok(menuService.toggleAvailability(id));
    }

    /**
     * DELETE /api/menu/{id}  [ADMIN]
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteItem(@PathVariable Long id) {
        menuService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Item deleted"));
    }

    /**
     * GET /api/menu/stalls
     */
    @GetMapping("/stalls")
    public ResponseEntity<?> getStalls() {
        return ResponseEntity.ok(menuService.getAllStalls());
    }
}
