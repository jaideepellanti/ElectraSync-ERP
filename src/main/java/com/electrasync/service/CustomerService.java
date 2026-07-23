package com.electrasync.service;

import com.electrasync.model.Customer;
import com.electrasync.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Optional<Customer> getById(Long id) {
        return customerRepository.findById(id);
    }

    public Optional<Customer> findByPhone(String phone) {
        return customerRepository.findByPhone(phone);
    }

    // Validates and saves a customer. Checks for duplicate phone number.
    @Transactional
    public Customer save(Customer customer) {
        Optional<Customer> existing = customerRepository.findByPhone(customer.getPhone());
        if (existing.isPresent() && !existing.get().getId().equals(customer.getId())) {
            throw new IllegalArgumentException("A customer with phone number '" + customer.getPhone() + "' already exists.");
        }
        return customerRepository.save(customer);
    }

    @Transactional
    public void delete(Long id) {
        customerRepository.deleteById(id);
    }
}
