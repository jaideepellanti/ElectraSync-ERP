package com.electrasync.controller;

import com.electrasync.model.PurchaseOrder;
import com.electrasync.model.PurchaseOrderItem;
import com.electrasync.service.ProductService;
import com.electrasync.service.PurchaseOrderService;
import com.electrasync.service.VendorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

@Controller
@RequestMapping("/manager/purchase-orders")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;
    private final VendorService vendorService;
    private final ProductService productService;

    public PurchaseOrderController(PurchaseOrderService purchaseOrderService,
                                   VendorService vendorService,
                                   ProductService productService) {
        this.purchaseOrderService = purchaseOrderService;
        this.vendorService = vendorService;
        this.productService = productService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("orders", purchaseOrderService.getAll());
        return "manager/po-list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        PurchaseOrder po = new PurchaseOrder();
        po.setOrderDate(LocalDate.now());
        po.setItems(new ArrayList<>());
        model.addAttribute("po", po);
        model.addAttribute("vendors", vendorService.getActiveVendors());
        model.addAttribute("products", productService.getAllActiveProducts());
        return "manager/po-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute PurchaseOrder po, RedirectAttributes redirectAttributes) {
        if (po.getItems() == null || po.getItems().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please add at least one product to the order.");
            return "redirect:/manager/purchase-orders/new";
        }
        try {
            po.setPoNumber(purchaseOrderService.generatePoNumber());
            // Set back-reference on each item
            if (po.getItems() != null) {
                for (PurchaseOrderItem item : po.getItems()) {
                    item.setPurchaseOrder(po);
                }
            }
            purchaseOrderService.createOrder(po);
            redirectAttributes.addFlashAttribute("successMessage", "Purchase Order created: " + po.getPoNumber());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating order: " + e.getMessage());
        }
        return "redirect:/manager/purchase-orders";
    }

    @GetMapping("/view/{id}")
    public String view(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        PurchaseOrder po = purchaseOrderService.getById(id).orElse(null);
        if (po == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Purchase Order not found.");
            return "redirect:/manager/purchase-orders";
        }
        model.addAttribute("po", po);
        return "manager/po-view";
    }

    @PostMapping("/receive/{id}")
    public String receive(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            purchaseOrderService.receiveOrder(id);
            redirectAttributes.addFlashAttribute("successMessage", "Order marked as received. Stock has been updated.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/manager/purchase-orders/view/" + id;
    }

    @PostMapping("/pay/{id}")
    public String recordPayment(@PathVariable Long id,
                                @RequestParam BigDecimal amount,
                                RedirectAttributes redirectAttributes) {
        try {
            purchaseOrderService.recordPayment(id, amount);
            redirectAttributes.addFlashAttribute("successMessage", "Payment of ₹" + amount + " recorded.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/manager/purchase-orders/view/" + id;
    }

    @PostMapping("/cancel/{id}")
    public String cancel(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            purchaseOrderService.cancelOrder(id);
            redirectAttributes.addFlashAttribute("successMessage", "Order cancelled.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/manager/purchase-orders";
    }
}
