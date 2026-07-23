package com.electrasync.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchase_orders")
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String poNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    @Column(nullable = false)
    private LocalDate orderDate;

    private LocalDate deliveryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private POStatus status = POStatus.PENDING;

    @Column(precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(precision = 12, scale = 2)
    private BigDecimal amountPaid = BigDecimal.ZERO;

    @Column(length = 500)
    private String notes;

    // Use LAZY to avoid loading all items on the list page
    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PurchaseOrderItem> items = new ArrayList<>();

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum POStatus {
        PENDING, ORDERED, RECEIVED, PARTIALLY_RECEIVED, CANCELLED
    }

    // Calculates how much is still owed to the vendor
    public BigDecimal getAmountDue() {
        if (totalAmount == null) return BigDecimal.ZERO;
        BigDecimal paid = amountPaid == null ? BigDecimal.ZERO : amountPaid;
        return totalAmount.subtract(paid);
    }

    // Getters
    public Long getId() { return id; }
    public String getPoNumber() { return poNumber; }
    public Vendor getVendor() { return vendor; }
    public LocalDate getOrderDate() { return orderDate; }
    public LocalDate getDeliveryDate() { return deliveryDate; }
    public POStatus getStatus() { return status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public BigDecimal getAmountPaid() { return amountPaid; }
    public String getNotes() { return notes; }
    public List<PurchaseOrderItem> getItems() { return items; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setPoNumber(String poNumber) { this.poNumber = poNumber; }
    public void setVendor(Vendor vendor) { this.vendor = vendor; }
    public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }
    public void setDeliveryDate(LocalDate deliveryDate) { this.deliveryDate = deliveryDate; }
    public void setStatus(POStatus status) { this.status = status; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public void setAmountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setItems(List<PurchaseOrderItem> items) { this.items = items; }
}
