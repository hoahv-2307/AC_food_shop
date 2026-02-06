package com.foodshop.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Stripe payment configuration.
 *
 * <p>Initializes the Stripe API with the secret key.
 */
@Configuration
public class StripeConfig {

  @Value("${app.stripe.secret-key}")
  private String secretKey;

  /**
   * Initializes Stripe API with the secret key.
   */
  @PostConstruct
  public void init() {
    Stripe.apiKey = secretKey;
  }
}
