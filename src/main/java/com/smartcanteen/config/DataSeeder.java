package com.smartcanteen.config;

import com.smartcanteen.entity.*;
import com.smartcanteen.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * DataSeeder — runs once on startup to populate the DB with stalls, menu items
 * (matching your frontend), inventory, and a default admin account.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

	private final UserRepository userRepository;
	private final StallRepository stallRepository;
	private final MenuItemRepository menuItemRepository;
	private final InventoryRepository inventoryRepository;
	private final PasswordEncoder passwordEncoder;

	@Override
	public void run(String... args) {

		// ── Stalls ────────────────────────────────────────────
		if (stallRepository.count() == 0) {
			List<Stall> stalls = List.of(new Stall(null, "Main Counter", "Block A - Ground Floor", true, 0),
					new Stall(null, "Snacks Counter", "Block B - Ground Floor", true, 0),
					new Stall(null, "Beverages", "Block A - First Floor", true, 0),
					new Stall(null, "South Indian", "Block C - Ground Floor", true, 0));
			stallRepository.saveAll(stalls);
			System.out.println("✅ Seeded 4 stalls");
		}

		// ── Menu Items ────────────────────────────────────────
		if (menuItemRepository.count() == 0) {
			Stall main = stallRepository.findAll().get(0);
			Stall snacks = stallRepository.findAll().get(1);
			Stall bev = stallRepository.findAll().get(2);
			Stall south = stallRepository.findAll().get(3);

			String cat = "Main Course (North Indian)";
			List<MenuItem> items = List.of(item(main, "Dal Tadka", cat, 63, true),
					item(main, "Dal Makhani", cat, 151, true), item(main, "Rajma", cat, 115, true),
					item(main, "Chole", cat, 151, true), item(main, "Kadhi Pakora", cat, 226, true),
					item(main, "Shahi Paneer", cat, 171, true), item(main, "Palak Paneer", cat, 169, true),
					item(main, "Matar Paneer", cat, 242, true), item(main, "Kadai Paneer", cat, 112, true),
					item(main, "Malai Kofta", cat, 239, true), item(main, "Paneer Butter Masala", cat, 171, true),
					item(main, "Bhindi Masala", cat, 97, true), item(main, "Aloo Gobi", cat, 87, true),
					item(main, "Dum Aloo", cat, 69, true), item(main, "Baingan Bharta", cat, 143, true),
					item(snacks, "Veg Burger", "Snacks", 60, true), item(snacks, "Pizza", "Snacks", 120, true),
					item(bev, "Cold Drink", "Beverages", 40, true), item(bev, "Lassi", "Beverages", 50, true),
					item(south, "Hyderabadi Biryani", "Biryani", 180, false),
					item(south, "Veg Biryani", "Biryani", 150, true),
					item(south, "Chicken Biryani", "Biryani", 200, false),
					item(south, "Mutton Biryani", "Biryani", 250, false));
			menuItemRepository.saveAll(items);
			System.out.println("✅ Seeded " + items.size() + " menu items");
		}

		// ── Inventory ─────────────────────────────────────────
		if (inventoryRepository.count() == 0) {
			List<Inventory> inv = List.of(inv("Bread", 20, "pieces", 5), inv("Cheese", 8, "slices", 5),
					inv("Tomato", 4, "kg", 5), inv("Cold Drink Syrup", 15, "litres", 5), inv("Rice", 50, "kg", 10),
					inv("Paneer", 10, "kg", 5), inv("Onion", 30, "kg", 8), inv("Oil", 20, "litres", 5));
			inventoryRepository.saveAll(inv);
			System.out.println("✅ Seeded inventory");
		}

		// ── Default Admin ─────────────────────────────────────
		if (!userRepository.existsByEmail("admin@canteen.com")) {
			userRepository.save(
					User.builder().name("Admin").email("admin@canteen.com").password(passwordEncoder.encode("admin123"))
							.role(User.Role.ADMIN).walletBalance(BigDecimal.ZERO).build());
			System.out.println("✅ Default admin created  →  admin@canteen.com / admin123");
		}
	}

	// ── helpers ──────────────────────────────────────────────
	private MenuItem item(Stall stall, String name, String category, int price, boolean veg) {
		return MenuItem.builder().stall(stall).name(name).category(category).price(BigDecimal.valueOf(price)).veg(veg)
				.available(true).build();
	}

	private Inventory inv(String name, int qty, String unit, int threshold) {
		return Inventory.builder().materialName(name).stockQuantity(qty).unit(unit).lowStockThreshold(threshold)
				.build();
	}
}
