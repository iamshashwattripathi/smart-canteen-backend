package com.smartcanteen.controller;

import com.smartcanteen.service.AISuggestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/suggest")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AISuggestionController {

    private final AISuggestionService aiSuggestionService;

    /**
     * GET /api/suggest
     * Returns the recommended food item based on order history.
     * Powers the AI Food Suggestion page.
     *
     * Response:
     * {
     *   "recommended": "Veg Burger",
     *   "reason": "Most ordered item with 3 orders",
     *   "totalOrders": 3,
     *   "allTopItems": [...]
     * }
     */
    @GetMapping
    public ResponseEntity<?> getSuggestion() {
        return ResponseEntity.ok(aiSuggestionService.getRecommendation());
    }
}
