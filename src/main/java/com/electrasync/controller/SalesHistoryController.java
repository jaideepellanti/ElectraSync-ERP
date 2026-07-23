package com.electrasync.controller;

import com.electrasync.service.SaleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/sales")
public class SalesHistoryController {

    private final SaleService saleService;

    public SalesHistoryController(SaleService saleService) {
        this.saleService = saleService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String from,
                       @RequestParam(required = false) String to,
                       Model model) {

        // Default to today's sales if no date range provided
        LocalDateTime fromDate = from != null ? LocalDate.parse(from).atStartOfDay() : LocalDate.now().atStartOfDay();
        LocalDateTime toDate = to != null ? LocalDate.parse(to).atTime(23, 59, 59) : LocalDateTime.now();

        model.addAttribute("sales", saleService.getSalesBetween(fromDate, toDate));
        model.addAttribute("from", from != null ? from : LocalDate.now().toString());
        model.addAttribute("to", to != null ? to : LocalDate.now().toString());
        return "sales/history";
    }

    @PostMapping("/return/{id}")
    public String returnSale(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            saleService.returnSale(id);
            redirectAttributes.addFlashAttribute("successMessage", "Sale returned successfully. Stock has been restored.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/sales";
    }
}
