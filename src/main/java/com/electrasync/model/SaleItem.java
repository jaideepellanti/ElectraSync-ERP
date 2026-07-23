package com.electrasync.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "sale_items")
public class SaleItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    // Snapshot of selling price at time of sale — so historical invoices stay accurate
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    // Snapshot of cost price at time of sale — used for profit calculation
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitCost;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal lineTotal;

    // Profit earned from this line: (sellingPrice - costPrice) * quantity
    public BigDecimal getLineProfit() {
        return unitPrice.subtract(unitCost).multiply(BigDecimal.valueOf(quantity));
    }

    // Getters
    public Long getId() { return id; }
    public Sale getSale() { return sale; }
    public Product getProduct() { return product; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getUnitCost() { return unitCost; }
    public BigDecimal getLineTotal() { return lineTotal; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setSale(Sale sale) { this.sale = sale; }
    public void setProduct(Product product) { this.product = product; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }
    public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }
}
