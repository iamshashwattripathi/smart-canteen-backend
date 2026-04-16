package com.smartcanteen.controller;

import com.smartcanteen.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	/**
	 * POST /api/auth/register Body: { "name":"Harsh", "email":"harsh@ucer.ac.in",
	 * "password":"pass123", "phone":"9876543210" }
	 */
	@PostMapping("/register")
	public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
		try {
			Map<String, Object> result = authService.register(body.get("name"), body.get("email"), body.get("password"),
					body.get("phone"));
			return ResponseEntity.ok(result);
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}

	/**
	 * POST /api/auth/login Body: { "email":"harsh@ucer.ac.in", "password":"pass123"
	 * }
	 */
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
		try {
			Map<String, Object> result = authService.login(body.get("email"), body.get("password"));
			return ResponseEntity.ok(result);
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}

	/**
	 * POST /api/auth/wallet/topup Body: { "userId": 1, "amount": 200.00 }
	 */
	@PostMapping("/wallet/topup")
	public ResponseEntity<?> topUpWallet(@RequestBody Map<String, Object> body) {
		try {
			Long userId = Long.parseLong(body.get("userId").toString());
			BigDecimal amount = new BigDecimal(body.get("amount").toString());
			var user = authService.topUpWallet(userId, amount);
			return ResponseEntity
					.ok(Map.of("message", "Wallet topped up successfully", "walletBalance", user.getWalletBalance()));
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}
}
