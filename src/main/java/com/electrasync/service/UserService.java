package com.electrasync.service;

import com.electrasync.model.User;
import com.electrasync.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getById(Long id) {
        return userRepository.findById(id);
    }

    // Used by POSController to find which user (cashier) is currently logged in
    public Optional<User> getByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public boolean isUsernameTaken(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean isEmailTaken(String email) {
        return userRepository.existsByEmail(email);
    }

    // Used when editing a user - checks if the email is used by a DIFFERENT user
    public boolean isEmailTakenByAnotherUser(String email, Long currentUserId) {
        return userRepository.findByEmail(email)
                .map(existingUser -> !existingUser.getId().equals(currentUserId))
                .orElse(false);
    }

    // Creates a new user with encrypted password
    @Transactional
    public User createUser(User user, String rawPassword) {
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setEnabled(true);
        return userRepository.save(user);
    }

    // Updates an existing user's details (name, email, role).
    // Password is only changed if a new one is provided - leaving it blank keeps the old password.
    // Username cannot be changed here since it's used to identify the account across the system.
    @Transactional
    public User updateUser(Long id, String fullName, String email, User.Role role, String newRawPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        user.setFullName(fullName);
        user.setEmail(email);
        user.setRole(role);

        if (newRawPassword != null && !newRawPassword.trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(newRawPassword));
        }

        return userRepository.save(user);
    }

    @Transactional
    public void disableUser(Long id) {
        userRepository.findById(id).ifPresent(user -> {
            user.setEnabled(false);
            userRepository.save(user);
        });
    }

    @Transactional
    public void enableUser(Long id) {
        userRepository.findById(id).ifPresent(user -> {
            user.setEnabled(true);
            userRepository.save(user);
        });
    }
}
