package com.electrasync.controller;

import com.electrasync.service.EmployeeService;
import com.electrasync.service.SaleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Controller
@RequestMapping("/owner/reports")
public class ReportController {

    private final SaleService saleService;
    private final EmployeeService employeeService;

    public ReportController(SaleService saleService, EmployeeService employeeService) {
        this.saleService = saleService;
        this.employeeService = employeeService;
    }

    @GetMapping("/profit-loss")
    public String profitLoss(@RequestParam(required = false) Integer month,
                              @RequestParam(required = false) Integer year,
                              Model model) {

        // Default to current month if no filters selected
        YearMonth ym = (month != null && year != null) ? YearMonth.of(year, month) : YearMonth.now();

        LocalDateTime start = ym.atDay(1).atStartOfDay();
        LocalDateTime end = ym.atEndOfMonth().atTime(23, 59, 59);

        BigDecimal revenue = saleService.getRevenueBetween(start, end);
        BigDecimal grossProfit = saleService.getProfitBetween(start, end);
        BigDecimal salaryExpense = employeeService.getTotalSalaryPaidForMonth(ym.getMonthValue(), ym.getYear());
        BigDecimal netProfit = grossProfit.subtract(salaryExpense);

        model.addAttribute("yearMonth", ym);
        model.addAttribute("revenue", revenue);
        model.addAttribute("grossProfit", grossProfit);
        model.addAttribute("salaryExpense", salaryExpense);
        model.addAttribute("netProfit", netProfit);
        model.addAttribute("salesList", saleService.getSalesBetween(start, end));

        return "owner/profit-loss";
    }
}
