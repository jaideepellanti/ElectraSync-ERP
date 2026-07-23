package com.electrasync.controller;

import com.electrasync.model.Employee;
import com.electrasync.model.SalaryPayment;
import com.electrasync.service.EmployeeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;

@Controller
@RequestMapping("/owner/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("employees", employeeService.getAllEmployees());
        return "owner/employees-list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("employee", new Employee());
        return "owner/employee-form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Employee employee = employeeService.getById(id).orElse(null);
        if (employee == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Employee not found.");
            return "redirect:/owner/employees";
        }
        model.addAttribute("employee", employee);
        return "owner/employee-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Employee employee, RedirectAttributes redirectAttributes) {
        try {
            employeeService.save(employee);
            redirectAttributes.addFlashAttribute("successMessage", "Employee '" + employee.getFullName() + "' saved successfully.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/owner/employees";
    }

    @GetMapping("/{id}/salary")
    public String salaryHistory(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Employee employee = employeeService.getById(id).orElse(null);
        if (employee == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Employee not found.");
            return "redirect:/owner/employees";
        }

        // Pre-fill the payment form with this month's details and the employee's standard salary
        SalaryPayment newPayment = new SalaryPayment();
        newPayment.setEmployee(employee);
        newPayment.setBasicSalary(employee.getMonthlySalary());
        newPayment.setPaymentDate(LocalDate.now());
        newPayment.setPayMonth(LocalDate.now().getMonthValue());
        newPayment.setPayYear(LocalDate.now().getYear());

        model.addAttribute("employee", employee);
        model.addAttribute("payments", employeeService.getSalaryHistory(id));
        model.addAttribute("newPayment", newPayment);
        return "owner/salary-history";
    }

    // Salary calculation moved to EmployeeService where it belongs
    @PostMapping("/salary/pay")
    public String paySalary(@ModelAttribute SalaryPayment payment, RedirectAttributes redirectAttributes) {
        try {
            employeeService.paySalary(payment);
            redirectAttributes.addFlashAttribute("successMessage", "Salary paid successfully.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/owner/employees/" + payment.getEmployee().getId() + "/salary";
    }
}
