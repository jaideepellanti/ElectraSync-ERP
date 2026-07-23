package com.electrasync.repository;

import com.electrasync.model.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, Long> {

    // Used to check for duplicate brand names before saving
    Optional<Brand> findByNameIgnoreCase(String name);
}
