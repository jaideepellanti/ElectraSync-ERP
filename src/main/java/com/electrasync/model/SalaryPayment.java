package com.electrasync.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "salary_payments")
public class SalaryPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private int payMonth;

    @Column(nullable = false)
    private int payYear;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal basicSalary;

    @Column(precision = 10, scale = 2)
    private BigDecimal bonus = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal deductions = BigDecimal.ZERO;

    // Net salary = basicSalary + bonus - deductions
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal netSalary;

    @Column(nullable = false)
    private LocalDate paymentDate;

    @Enumerated(EnumType.STRING)
    private PaymentMode paymentMode;

    // Transaction/reference ID for digital payments (UPI reference, NEFT/IMPS number, cheque number).
    // Not required for CASH payments.
    @Column(length = 50)
    private String paymentReference;

    private String remarks;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum PaymentMode {
        CASH, UPI, BANK_TRANSFER, CHEQUE
    }

    // Getters
    public Long getId() { return id; }
    public Employee getEmployee() { return employee; }
    public int getPayMonth() { return payMonth; }
    public int getPayYear() { return payYear; }
    public BigDecimal getBasicSalary() { return basicSalary; }
    public BigDecimal getBonus() { return bonus; }
    public BigDecimal getDeductions() { return deductions; }
    public BigDecimal getNetSalary() { return netSalary; }
    public LocalDate getPaymentDate() { return paymentDate; }
    public PaymentMode getPaymentMode() { return paymentMode; }
    public String getPaymentReference() { return paymentReference; }
    public String getRemarks() { return remarks; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    public void setPayMonth(int payMonth) { this.payMonth = payMonth; }
    public void setPayYear(int payYear) { this.payYear = payYear; }
    public void setBasicSalary(BigDecimal basicSalary) { this.basicSalary = basicSalary; }
    public void setBonus(BigDecimal bonus) { this.bonus = bonus; }
    public void setDeductions(BigDecimal deductions) { this.deductions = deductions; }
    public void setNetSalary(BigDecimal netSalary) { this.netSalary = netSalary; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }
    public void setPaymentMode(PaymentMode paymentMode) { this.paymentMode = paymentMode; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
