package com.foodshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main application class for Food Shop Application.
 * 
 * <p>This is an e-commerce platform for food items with features including:
 * - OAuth2 social authentication (Google, Facebook)
 * - Food catalog browsing and search
 * - Shopping cart and order management
 * - Payment processing via Stripe
 * - Product ratings and reviews
 * - Admin dashboard for management
 * - Email notifications
 * </p>
 *
 * @author Food Shop Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableAsync
public class FoodShopApplication {

    /**
     * Main entry point for the application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(FoodShopApplication.class, args);
    }
}
