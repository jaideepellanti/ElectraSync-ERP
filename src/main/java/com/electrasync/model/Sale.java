package com.electrasync.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sales")
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String invoiceNumber;

    // Null if walk-in customer (not registered)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    // Used when customer is not registered in the system
    @Column(length = 100)
    private String walkInCustomerName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cashier_id")
    private User cashier;

    @Column(nullable = false)
    private LocalDateTime saleDate;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subTotal;

    @Column(precision = 5, scale = 2)
    private BigDecimal discountPercent = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    // Reference/transaction ID for digital payments (UPI transaction ID, card approval code, etc.)
    // Not required for CASH payments.
    @Column(length = 50)
    private String paymentReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SaleStatus status = SaleStatus.COMPLETED;

    // Use LAZY to avoid loading all items when listing sales
    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SaleItem> saleItems = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (saleDate == null) saleDate = LocalDateTime.now();
    }

    public enum PaymentMethod {
        CASH, UPI, CARD, EMI, NET_BANKING
    }

    public enum SaleStatus {
        COMPLETED, RETURNED, PARTIALLY_RETURNED
    }

    // Getters
    public Long getId() { return id; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public Customer getCustomer() { return customer; }
    public String getWalkInCustomerName() { return walkInCustomerName; }
    public User getCashier() { return cashier; }
    public LocalDateTime getSaleDate() { return saleDate; }
    public BigDecimal getSubTotal() { return subTotal; }
    public BigDecimal getDiscountPercent() { return discountPercent; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public String getPaymentReference() { return paymentReference; }
    public SaleStatus getStatus() { return status; }
    public List<SaleItem> getSaleItems() { return saleItems; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public void setWalkInCustomerName(String walkInCustomerName) { this.walkInCustomerName = walkInCustomerName; }
    public void setCashier(User cashier) { this.cashier = cashier; }
    public void setSaleDate(LocalDateTime saleDate) { this.saleDate = saleDate; }
    public void setSubTotal(BigDecimal subTotal) { this.subTotal = subTotal; }
    public void setDiscountPercent(BigDecimal discountPercent) { this.discountPercent = discountPercent; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }
    public void setStatus(SaleStatus status) { this.status = status; }
    public void setSaleItems(List<SaleItem> saleItems) { this.saleItems = saleItems; }
}
