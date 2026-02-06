package com.foodshop.security;

import com.foodshop.domain.User;
import com.foodshop.repository.UserRepository;
import java.time.LocalDateTime;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

/**
 * Custom OAuth2 user service that handles user registration and updates.
 *
 * <p>This service loads OAuth2 user information from the provider and creates or updates the
 * corresponding User entity in the database.
 */
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

  private final UserRepository userRepository;

  public CustomOAuth2UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2User oauth2User = super.loadUser(userRequest);

    String registrationId = userRequest.getClientRegistration().getRegistrationId();
    User.OAuthProvider provider = User.OAuthProvider.valueOf(registrationId.toUpperCase());

    String externalId = oauth2User.getAttribute("sub");
    if (externalId == null) {
      externalId = oauth2User.getAttribute("id");
    }

    final String finalExternalId = externalId;
    User user = userRepository
        .findByProviderAndExternalId(provider, finalExternalId)
        .orElseGet(() -> createNewUser(oauth2User, provider, finalExternalId));

    // Update user information from OAuth2 provider
    updateUserFromOAuth2(user, oauth2User);
    userRepository.save(user);

    return new UserPrincipal(user, oauth2User.getAttributes());
  }

  private User createNewUser(OAuth2User oauth2User, User.OAuthProvider provider,
      String externalId) {
    User user = new User();
    user.setProvider(provider);
    user.setExternalId(externalId);
    user.setRole(User.UserRole.CUSTOMER);
    user.setStatus(User.UserStatus.ACTIVE);
    user.setCreatedAt(LocalDateTime.now());
    return user;
  }

  private void updateUserFromOAuth2(User user, OAuth2User oauth2User) {
    String email = oauth2User.getAttribute("email");
    String name = oauth2User.getAttribute("name");
    String picture = oauth2User.getAttribute("picture");

    if (email != null) {
      user.setEmail(email);
    }
    if (name != null) {
      user.setName(name);
    }
    if (picture != null) {
      user.setAvatarUrl(picture);
    }
    user.setUpdatedAt(LocalDateTime.now());
  }
}
