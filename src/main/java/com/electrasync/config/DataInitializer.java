package com.electrasync.config;

import com.electrasync.model.Product;
import com.electrasync.model.User;
import com.electrasync.repository.ProductRepository;
import com.electrasync.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * Runs once on every application startup to:
 * 1. Create a default OWNER account if no users exist yet.
 * 2. Repair any product records where minimumStock ended up null due to a
 *    previous form bug (an HTML placeholder was mistaken for a real default value).
 *    A null minimumStock silently breaks the low-stock alert query, so this
 *    keeps existing data consistent even if it was saved before the fix.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           ProductRepository productRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        createDefaultOwnerIfMissing();
        repairProductsWithMissingMinimumStock();
    }

    private void createDefaultOwnerIfMissing() {
        if (!userRepository.existsByUsername("owner")) {
            User owner = new User();
            owner.setUsername("owner");
            owner.setPassword(passwordEncoder.encode("ElectraSync@2026"));
            owner.setFullName("Store Owner");
            owner.setEmail("owner@electrasync.com");
            owner.setRole(User.Role.OWNER);
            owner.setEnabled(true);
            userRepository.save(owner);
            System.out.println("Default owner account created. Username: owner  Password: ElectraSync@2026");
        }
    }

    private void repairProductsWithMissingMinimumStock() {
        List<Product> allProducts = productRepository.findAll();
        int repairedCount = 0;

        for (Product product : allProducts) {
            boolean needsRepair = false;

            if (product.getMinimumStock() == null) {
                product.setMinimumStock(5);
                needsRepair = true;
            }
            if (product.getStockQuantity() == null) {
                product.setStockQuantity(0);
                needsRepair = true;
            }

            if (needsRepair) {
                productRepository.save(product);
                repairedCount++;
            }
        }

        if (repairedCount > 0) {
            System.out.println("Repaired " + repairedCount + " product(s) with missing stock threshold values.");
        }
    }
}
