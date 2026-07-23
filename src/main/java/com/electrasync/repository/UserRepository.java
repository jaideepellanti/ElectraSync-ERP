package com.electrasync.repository;

import com.electrasync.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    // Used when editing a user, to check if the email is taken by a DIFFERENT user
    Optional<User> findByEmail(String email);
}
