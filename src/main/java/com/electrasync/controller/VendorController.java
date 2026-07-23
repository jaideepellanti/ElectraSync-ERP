package com.electrasync.controller;

import com.electrasync.model.Vendor;
import com.electrasync.service.VendorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/manager/vendors")
public class VendorController {

    private final VendorService vendorService;

    public VendorController(VendorService vendorService) {
        this.vendorService = vendorService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("vendors", vendorService.getAllVendors());
        return "manager/vendors-list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("vendor", new Vendor());
        return "manager/vendor-form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Vendor vendor = vendorService.getById(id).orElse(null);
        if (vendor == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vendor not found.");
            return "redirect:/manager/vendors";
        }
        model.addAttribute("vendor", vendor);
        return "manager/vendor-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Vendor vendor, RedirectAttributes redirectAttributes) {
        try {
            vendorService.save(vendor);
            redirectAttributes.addFlashAttribute("successMessage", "Vendor '" + vendor.getCompanyName() + "' saved successfully.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/manager/vendors";
    }

    @PostMapping("/deactivate/{id}")
    public String deactivate(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            vendorService.deactivate(id);
            redirectAttributes.addFlashAttribute("successMessage", "Vendor deactivated successfully.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/manager/vendors";
    }
}
