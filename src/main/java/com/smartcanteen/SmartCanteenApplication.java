package com.smartcanteen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Smart Canteen Billing System - Main Application
 *
 * Web Matrix Project Department of Computer Science & Engineering United
 * College of Engineering & Research, Prayagraj Dr. APJ Abdul Kalam Technical
 * University, Lucknow May 2025
 *
 * Team: Harsh Srivastava (2100100100074) Harsh Pandey (2100100100072) Shaswat
 * Tripathi (21001001000151) Anubhav Srivastava (2200100100078)
 *
 * Supervisor: Mr. Bhanu Pratap Rai
 */
@SpringBootApplication
public class SmartCanteenApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartCanteenApplication.class, args);
		System.out.println("╔══════════════════════════════════════════════════╗");
		System.out.println("║   Smart Canteen Billing System - Server Started  ║");
		System.out.println("║   Running on: http://localhost:8080               ║");
		System.out.println("║   API Base:   http://localhost:8080/api           ║");
		System.out.println("╚══════════════════════════════════════════════════╝");
	}
}
