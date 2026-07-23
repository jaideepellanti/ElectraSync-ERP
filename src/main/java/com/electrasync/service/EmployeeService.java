package com.electrasync.service;

import com.electrasync.model.Employee;
import com.electrasync.model.SalaryPayment;
import com.electrasync.repository.EmployeeRepository;
import com.electrasync.repository.SalaryPaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final SalaryPaymentRepository salaryPaymentRepository;

    public EmployeeService(EmployeeRepository employeeRepository,
                           SalaryPaymentRepository salaryPaymentRepository) {
        this.employeeRepository = employeeRepository;
        this.salaryPaymentRepository = salaryPaymentRepository;
    }

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public Optional<Employee> getById(Long id) {
        return employeeRepository.findById(id);
    }

    // Validates and saves an employee. Checks for duplicate phone number.
    //
    // Joining date is locked once set - it should never change after the employee
    // record is created, so we always keep the original value on edit instead of
    // trusting whatever the form submitted.
    //
    // The linked login (user account) is also preserved here, because the edit form
    // does not include a field for it - without this, editing an employee would
    // silently remove their login link.
    @Transactional
    public Employee save(Employee employee) {
        Optional<Employee> existing = employeeRepository.findByPhone(employee.getPhone());
        if (existing.isPresent() && !existing.get().getId().equals(employee.getId())) {
            throw new IllegalArgumentException("An employee with phone number '" + employee.getPhone() + "' already exists.");
        }

        if (employee.getId() != null) {
            Employee currentEmployee = employeeRepository.findById(employee.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Employee not found."));
            employee.setJoiningDate(currentEmployee.getJoiningDate());
            employee.setUser(currentEmployee.getUser());
        }

        return employeeRepository.save(employee);
    }

    // Links a system login (User account) to this employee, so we know which
    // employee record corresponds to which login. Not every employee needs this —
    // only ones who will actually use the software (cashiers, managers).
    @Transactional
    public void linkUserAccount(Long employeeId, com.electrasync.model.User user) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found."));
        employee.setUser(user);
        employeeRepository.save(employee);
    }

    // Calculates net salary and saves the payment.
    // Net salary = basicSalary + bonus - deductions
    // Prevents paying the same employee twice in one month.
    // Requires a transaction reference for digital payment modes (UPI, Bank Transfer,
    // Cheque) so there is a way to verify the payment actually went through later —
    // matching how real payroll systems require proof of payment for anything non-cash.
    @Transactional
    public SalaryPayment paySalary(SalaryPayment payment) {
        Optional<SalaryPayment> alreadyPaid = salaryPaymentRepository.findByEmployeeIdAndPayMonthAndPayYear(
                payment.getEmployee().getId(), payment.getPayMonth(), payment.getPayYear());

        if (alreadyPaid.isPresent()) {
            throw new IllegalArgumentException("Salary for " + getMonthName(payment.getPayMonth())
                    + " " + payment.getPayYear() + " has already been paid for this employee.");
        }

        boolean requiresReference = payment.getPaymentMode() == SalaryPayment.PaymentMode.UPI
                || payment.getPaymentMode() == SalaryPayment.PaymentMode.BANK_TRANSFER
                || payment.getPaymentMode() == SalaryPayment.PaymentMode.CHEQUE;

        if (requiresReference && (payment.getPaymentReference() == null || payment.getPaymentReference().trim().isEmpty())) {
            throw new IllegalArgumentException("A transaction/reference number is required for " + payment.getPaymentMode() + " payments.");
        }

        BigDecimal bonus = payment.getBonus() == null ? BigDecimal.ZERO : payment.getBonus();
        BigDecimal deductions = payment.getDeductions() == null ? BigDecimal.ZERO : payment.getDeductions();
        payment.setNetSalary(payment.getBasicSalary().add(bonus).subtract(deductions));

        return salaryPaymentRepository.save(payment);
    }

    public List<SalaryPayment> getSalaryHistory(Long employeeId) {
        return salaryPaymentRepository.findByEmployeeId(employeeId);
    }

    // Returns total salary paid for a given month — used by the P&L report
    public BigDecimal getTotalSalaryPaidForMonth(int month, int year) {
        BigDecimal total = salaryPaymentRepository.totalSalaryForMonth(month, year);
        return total == null ? BigDecimal.ZERO : total;
    }

    private String getMonthName(int month) {
        String[] months = {"January","February","March","April","May","June",
                           "July","August","September","October","November","December"};
        return (month >= 1 && month <= 12) ? months[month - 1] : "Month " + month;
    }
}
