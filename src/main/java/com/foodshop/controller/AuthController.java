package com.foodshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Authentication controller handling login and logout.
 */
@Controller
public class AuthController {

  /**
   * Displays the login page.
   *
   * @return the login view name
   */
  @GetMapping("/login")
  public String login() {
    return "login";
  }

  /**
   * Handles successful OAuth2 login.
   *
   * @return redirect to catalog page
   */
  @GetMapping("/login/oauth2/success")
  public String loginSuccess() {
    return "redirect:/catalog";
  }

  /**
   * Handles failed OAuth2 login.
   *
   * @return redirect to login page with error
   */
  @GetMapping("/login/oauth2/failure")
  public String loginFailure() {
    return "redirect:/login?error=true";
  }
}
