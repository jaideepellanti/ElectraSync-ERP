package com.electrasync.service;

import com.electrasync.model.Vendor;
import com.electrasync.repository.VendorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class VendorService {

    private final VendorRepository vendorRepository;

    public VendorService(VendorRepository vendorRepository) {
        this.vendorRepository = vendorRepository;
    }

    public List<Vendor> getAllVendors() {
        return vendorRepository.findAll();
    }

    public List<Vendor> getActiveVendors() {
        return vendorRepository.findByActiveTrue();
    }

    public Optional<Vendor> getById(Long id) {
        return vendorRepository.findById(id);
    }

    // Validates and saves a vendor. Checks for duplicate phone number.
    @Transactional
    public Vendor save(Vendor vendor) {
        Optional<Vendor> existing = vendorRepository.findByPhone(vendor.getPhone());
        if (existing.isPresent() && !existing.get().getId().equals(vendor.getId())) {
            throw new IllegalArgumentException("A vendor with phone number '" + vendor.getPhone() + "' already exists.");
        }
        return vendorRepository.save(vendor);
    }

    @Transactional
    public void deactivate(Long id) {
        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vendor not found with id: " + id));
        vendor.setActive(false);
        vendorRepository.save(vendor);
    }
}
