package com.foodshop.config;

import com.foodshop.security.CustomOAuth2UserService;
import com.foodshop.security.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the application.
 *
 * <p>Configures OAuth2 login with Google and Facebook, form-based login for admin,
 * session management, and authorization rules.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  private final CustomOAuth2UserService customOAuth2UserService;
  private final CustomUserDetailsService customUserDetailsService;

  public SecurityConfig(
      CustomOAuth2UserService customOAuth2UserService,
      CustomUserDetailsService customUserDetailsService) {
    this.customOAuth2UserService = customOAuth2UserService;
    this.customUserDetailsService = customUserDetailsService;
  }

  /**
   * Password encoder bean for form-based authentication.
   *
   * @return BCrypt password encoder
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /**
   * Configures the security filter chain.
   *
   * @param http the HttpSecurity to configure
   * @return the configured SecurityFilterChain
   * @throws Exception if configuration fails
   */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers("/", "/catalog/**", "/food/**", "/css/**", "/js/**", "/images/**",
                "/error", "/login", "/oauth2/**", "/favicon.ico")
            .permitAll()
            .requestMatchers("/admin/**")
            .hasRole("ADMIN")
            .anyRequest()
            .authenticated()
        )
        .formLogin(form -> form
            .loginPage("/login")
            .loginProcessingUrl("/login")
            .defaultSuccessUrl("/admin/analytics", true)
            .failureUrl("/login?error=true")
            .usernameParameter("email")
            .passwordParameter("password")
            .permitAll()
        )
        .oauth2Login(oauth2 -> oauth2
            .loginPage("/login")
            .defaultSuccessUrl("/catalog", true)
            .userInfoEndpoint(userInfo -> userInfo
                .userService(customOAuth2UserService)
            )
        )
        .logout(logout -> logout
            .logoutSuccessUrl("/")
            .invalidateHttpSession(true)
            .deleteCookies("JSESSIONID")
        )
        .sessionManagement(session -> session
            .maximumSessions(1)
            .maxSessionsPreventsLogin(false)
        )
        .userDetailsService(customUserDetailsService);

    return http.build();
  }
}
