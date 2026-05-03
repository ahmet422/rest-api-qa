package com.mindtek.bookstore.security;

import com.mindtek.bookstore.domain.Role;
import com.mindtek.bookstore.domain.User;
import com.mindtek.bookstore.repo.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Order(10)
public class UserInitializer implements ApplicationRunner {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public UserInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public void run(ApplicationArguments args) {
    if (userRepository.count() > 0) {
      return;
    }
    User admin = new User();
    admin.setUsername("admin");
    admin.setPasswordHash(passwordEncoder.encode("password"));
    admin.setRole(Role.ADMIN);
    userRepository.save(admin);

    User user = new User();
    user.setUsername("user");
    user.setPasswordHash(passwordEncoder.encode("password"));
    user.setRole(Role.USER);
    userRepository.save(user);
  }
}
