package com.foodshop.security;

import com.foodshop.domain.User;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

/**
 * Custom user principal that implements both UserDetails and OAuth2User.
 *
 * <p>This class bridges the gap between Spring Security's OAuth2 authentication and the
 * application's User entity.
 */
public class UserPrincipal implements OAuth2User, UserDetails {

  private final User user;
  private final Map<String, Object> attributes;

  /**
   * Constructs a UserPrincipal from a User entity and OAuth2 attributes.
   *
   * @param user the user entity
   * @param attributes OAuth2 attributes from the provider
   */
  public UserPrincipal(User user, Map<String, Object> attributes) {
    this.user = user;
    this.attributes = attributes;
  }

  /**
   * Gets the User entity.
   *
   * @return the user entity
   */
  public User getUser() {
    return user;
  }

  @Override
  public String getName() {
    return user.getEmail();
  }

  @Override
  public Map<String, Object> getAttributes() {
    return attributes;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
  }

  @Override
  public String getPassword() {
    return null; // OAuth2 users don't have passwords
  }

  @Override
  public String getUsername() {
    return user.getEmail();
  }

  @Override
  public boolean isAccountNonExpired() {
    return user.getStatus() == User.UserStatus.ACTIVE;
  }

  @Override
  public boolean isAccountNonLocked() {
    return user.getStatus() != User.UserStatus.DEACTIVATED;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return user.getStatus() == User.UserStatus.ACTIVE;
  }
}
