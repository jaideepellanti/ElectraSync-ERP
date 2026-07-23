package com.electrasync.controller;

import com.electrasync.model.Customer;
import com.electrasync.service.CustomerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/manager/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("customers", customerService.getAllCustomers());
        return "manager/customers-list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("customer", new Customer());
        return "manager/customer-form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Customer customer = customerService.getById(id).orElse(null);
        if (customer == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Customer not found.");
            return "redirect:/manager/customers";
        }
        model.addAttribute("customer", customer);
        return "manager/customer-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Customer customer, RedirectAttributes redirectAttributes) {
        try {
            customerService.save(customer);
            redirectAttributes.addFlashAttribute("successMessage", "Customer '" + customer.getFullName() + "' saved successfully.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/manager/customers";
    }

    // Changed from GET to POST to prevent accidental deletion from browser prefetch
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            customerService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Customer deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot delete customer. They may have existing sales records.");
        }
        return "redirect:/manager/customers";
    }
}
