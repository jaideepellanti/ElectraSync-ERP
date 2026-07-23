package com.electrasync.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "purchase_order_items")
public class PurchaseOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitCostPrice;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal lineTotal;

    // Getters
    public Long getId() { return id; }
    public PurchaseOrder getPurchaseOrder() { return purchaseOrder; }
    public Product getProduct() { return product; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getUnitCostPrice() { return unitCostPrice; }
    public BigDecimal getLineTotal() { return lineTotal; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setPurchaseOrder(PurchaseOrder purchaseOrder) { this.purchaseOrder = purchaseOrder; }
    public void setProduct(Product product) { this.product = product; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public void setUnitCostPrice(BigDecimal unitCostPrice) { this.unitCostPrice = unitCostPrice; }
    public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }
}
