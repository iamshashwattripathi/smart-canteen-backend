package com.smartcanteen.service;

import com.smartcanteen.entity.User;
import com.smartcanteen.entity.User.Role;
import com.smartcanteen.repository.UserRepository;
import com.smartcanteen.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtil jwtUtil;

	// ── Register ─────────────────────────────────────────────
	public Map<String, Object> register(String name, String email, String password, String phone) {
		if (userRepository.existsByEmail(email)) {
			throw new RuntimeException("Email already registered");
		}

		User user = User.builder().name(name).email(email).password(passwordEncoder.encode(password)).phone(phone)
				.role(Role.CUSTOMER).build();

		User saved = userRepository.save(user);
		String token = jwtUtil.generateToken(saved.getEmail(), saved.getRole().name());

		return buildResponse(saved, token);
	}

	// ── Login ────────────────────────────────────────────────
	public Map<String, Object> login(String email, String password) {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new RuntimeException("Invalid email or password"));

		if (!passwordEncoder.matches(password, user.getPassword())) {
			throw new RuntimeException("Invalid email or password");
		}

		String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
		return buildResponse(user, token);
	}

	// ── Wallet: top up ───────────────────────────────────────
	public User topUpWallet(Long userId, java.math.BigDecimal amount) {
		User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
		user.setWalletBalance(user.getWalletBalance().add(amount));
		return userRepository.save(user);
	}

	// ── Helper ───────────────────────────────────────────────
	private Map<String, Object> buildResponse(User user, String token) {
		Map<String, Object> res = new HashMap<>();
		res.put("token", token);
		res.put("userId", user.getId());
		res.put("name", user.getName());
		res.put("email", user.getEmail());
		res.put("role", user.getRole());
		res.put("walletBalance", user.getWalletBalance());
		return res;
	}
}
