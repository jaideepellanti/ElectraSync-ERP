package com.electrasync.repository;

import com.electrasync.model.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface VendorRepository extends JpaRepository<Vendor, Long> {

    List<Vendor> findByActiveTrue();

    // Used to check for duplicate phone numbers before saving a vendor
    Optional<Vendor> findByPhone(String phone);
}
