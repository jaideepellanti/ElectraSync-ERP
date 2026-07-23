package com.electrasync.controller;

import com.electrasync.model.Product;
import com.electrasync.model.Sale;
import com.electrasync.model.SaleItem;
import com.electrasync.model.User;
import com.electrasync.service.CustomerService;
import com.electrasync.service.ProductService;
import com.electrasync.service.SaleService;
import com.electrasync.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/pos")
public class POSController {

    private final ProductService productService;
    private final CustomerService customerService;
    private final SaleService saleService;
    private final UserService userService;

    public POSController(ProductService productService,
                         CustomerService customerService,
                         SaleService saleService,
                         UserService userService) {
        this.productService = productService;
        this.customerService = customerService;
        this.saleService = saleService;
        this.userService = userService;
    }

    @GetMapping
    public String billingScreen(Model model) {
        model.addAttribute("products", productService.getAllActiveProducts());
        model.addAttribute("categories", productService.getAllCategories());
        model.addAttribute("brands", productService.getAllBrands());
        return "pos/billing";
    }

    // Product search endpoint - called from the search box on the POS screen
    @GetMapping("/search")
    @ResponseBody
    public List<Product> searchProducts(@RequestParam String q) {
        return productService.searchByName(q);
    }

    // Customer lookup by phone number - called via JavaScript when cashier types a phone number
    @GetMapping("/customer-search")
    @ResponseBody
    public ResponseEntity<?> findCustomerByPhone(@RequestParam String phone) {
        return customerService.findByPhone(phone)
                .<ResponseEntity<?>>map(customer -> ResponseEntity.ok(
                        Map.of("id", customer.getId(),
                               "fullName", customer.getFullName(),
                               "phone", customer.getPhone())
                ))
                .orElse(ResponseEntity.ok(Map.of()));
    }

    @PostMapping("/checkout")
    public String checkout(@RequestParam List<Long> productIds,
                           @RequestParam List<Integer> quantities,
                           @RequestParam(required = false) Long customerId,
                           @RequestParam(required = false) String walkInCustomerName,
                           @RequestParam(required = false) BigDecimal discountPercent,
                           @RequestParam Sale.PaymentMethod paymentMethod,
                           @RequestParam(required = false) String paymentReference,
                           Authentication auth,
                           Model model) {

        // Build sale items from the submitted product IDs and quantities
        Sale sale = new Sale();
        List<SaleItem> items = new ArrayList<>();

        for (int i = 0; i < productIds.size(); i++) {
            Product product = productService.getById(productIds.get(i)).orElse(null);
            if (product == null) continue;

            SaleItem item = new SaleItem();
            item.setProduct(product);
            item.setQuantity(quantities.get(i));
            items.add(item);
        }

        sale.setSaleItems(items);
        sale.setDiscountPercent(discountPercent == null ? BigDecimal.ZERO : discountPercent);
        sale.setPaymentMethod(paymentMethod);

        // A reference number (UPI transaction ID, card approval code) is required for
        // digital payments so there is a way to trace the transaction later if needed.
        boolean isDigitalPayment = paymentMethod == Sale.PaymentMethod.UPI
                || paymentMethod == Sale.PaymentMethod.CARD
                || paymentMethod == Sale.PaymentMethod.NET_BANKING;

        if (isDigitalPayment && (paymentReference == null || paymentReference.trim().isEmpty())) {
            model.addAttribute("errorMessage", "Please enter a transaction/reference number for " + paymentMethod + " payments.");
            model.addAttribute("products", productService.getAllActiveProducts());
            model.addAttribute("categories", productService.getAllCategories());
            model.addAttribute("brands", productService.getAllBrands());
            return "pos/billing";
        }
        sale.setPaymentReference(paymentReference != null ? paymentReference.trim() : null);

        // Link to registered customer if selected, otherwise use walk-in name
        if (customerId != null) {
            sale.setCustomer(customerService.getById(customerId).orElse(null));
        }
        if (walkInCustomerName != null && !walkInCustomerName.trim().isEmpty()) {
            sale.setWalkInCustomerName(walkInCustomerName.trim());
        }

        // Record which cashier processed this sale
        User cashier = userService.getByUsername(auth.getName()).orElse(null);
        sale.setCashier(cashier);

        try {
            Sale savedSale = saleService.processCheckout(sale);
            model.addAttribute("sale", savedSale);
            return "pos/invoice";
        } catch (IllegalStateException e) {
            // Stock validation failed - show error on billing screen
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("products", productService.getAllActiveProducts());
            model.addAttribute("categories", productService.getAllCategories());
            model.addAttribute("brands", productService.getAllBrands());
            return "pos/billing";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Something went wrong. Please try again.");
            model.addAttribute("products", productService.getAllActiveProducts());
            model.addAttribute("categories", productService.getAllCategories());
            model.addAttribute("brands", productService.getAllBrands());
            return "pos/billing";
        }
    }

    @GetMapping("/invoice/{id}")
    public String viewInvoice(@PathVariable Long id, Model model) {
        Sale sale = saleService.getById(id).orElse(null);
        if (sale == null) {
            return "redirect:/pos";
        }
        model.addAttribute("sale", sale);
        return "pos/invoice";
    }
}
