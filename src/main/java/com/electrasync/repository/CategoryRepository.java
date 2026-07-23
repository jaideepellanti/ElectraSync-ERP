package com.electrasync.repository;

import com.electrasync.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Used to check for duplicate category names before saving
    Optional<Category> findByNameIgnoreCase(String name);
}
