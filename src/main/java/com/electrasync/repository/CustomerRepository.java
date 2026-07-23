package com.electrasync.repository;

import com.electrasync.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // Used for the POS customer lookup and duplicate phone check
    Optional<Customer> findByPhone(String phone);
}
