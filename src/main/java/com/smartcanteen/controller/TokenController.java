package com.smartcanteen.controller;

import com.smartcanteen.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/tokens")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class TokenController {

	private final TokenService tokenService;

	/**
	 * POST /api/tokens/generate Generates the next queue token number for today.
	 * Powers the Token Generator page.
	 */
	@PostMapping("/generate")
	public ResponseEntity<?> generate() {
		return ResponseEntity.ok(tokenService.generateToken());
	}

	/**
	 * GET /api/tokens/queue Returns waiting / serving / done counts.
	 */
	@GetMapping("/queue")
	public ResponseEntity<?> queueCounts() {
		return ResponseEntity.ok(tokenService.getQueueCounts());
	}

	/**
	 * PATCH /api/tokens/{id}/status [STAFF] Body: { "status": "SERVING" }
	 */
	@PatchMapping("/{id}/status")
	public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
		try {
			var token = tokenService.updateStatus(id, body.get("status"));
			return ResponseEntity.ok(Map.of("tokenId", token.getId(), "tokenNumber", token.getTokenNumber(), "status",
					token.getStatus()));
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}
}
