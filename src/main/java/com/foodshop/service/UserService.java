package com.foodshop.service;

import com.foodshop.domain.User;
import com.foodshop.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing users.
 */
@Service
@Transactional(readOnly = true)
public class UserService {

  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  /**
   * Finds all users with ADMIN role.
   *
   * @return list of admin users
   */
  public List<User> findAdminUsers() {
    return userRepository.findByRole(User.UserRole.ADMIN);
  }
}
