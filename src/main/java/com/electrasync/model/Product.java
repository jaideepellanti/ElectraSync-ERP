package com.electrasync.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // SKU - unique stock keeping unit code for each product
    @Column(nullable = false, unique = true, length = 30)
    private String sku;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    // Price we paid the vendor to procure this product
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal costPrice;

    // Price we sell to the customer
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal sellingPrice;

    @Column(nullable = false)
    private Integer stockQuantity = 0;

    // When stock falls to or below this number, it appears in low-stock alerts
    @Column(nullable = false)
    private Integer minimumStock = 5;

    @Column(nullable = false)
    private boolean active = true;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    // Category-specific attributes for this product (e.g. RAM, Storage, Color for a phone).
    // EAGER because these are shown on the POS billing screen and product list right away.
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ProductSpecification> specifications = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Returns true if current stock is at or below the minimum threshold
    public boolean isLowStock() {
        return stockQuantity != null && minimumStock != null && stockQuantity <= minimumStock;
    }

    // Calculates profit margin percentage: ((sellingPrice - costPrice) / sellingPrice) * 100
    public BigDecimal getProfitMargin() {
        if (costPrice == null || sellingPrice == null || sellingPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return sellingPrice.subtract(costPrice)
                .divide(sellingPrice, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    // Getters
    public Long getId() { return id; }
    public String getSku() { return sku; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Category getCategory() { return category; }
    public Brand getBrand() { return brand; }
    public BigDecimal getCostPrice() { return costPrice; }
    public BigDecimal getSellingPrice() { return sellingPrice; }
    public Integer getStockQuantity() { return stockQuantity; }
    public Integer getMinimumStock() { return minimumStock; }
    public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<ProductSpecification> getSpecifications() { return specifications; }

    // Builds a short display string like "RAM: 8GB, Storage: 128GB, Color: Black"
    // Used in the POS billing screen and invoice so the cashier/customer can see exactly
    // which variant of the product this is.
    public String getSpecificationSummary() {
        if (specifications == null || specifications.isEmpty()) {
            return "";
        }
        StringBuilder summary = new StringBuilder();
        for (int i = 0; i < specifications.size(); i++) {
            ProductSpecification spec = specifications.get(i);
            summary.append(spec.getSpecName()).append(": ").append(spec.getSpecValue());
            if (i < specifications.size() - 1) {
                summary.append(", ");
            }
        }
        return summary.toString();
    }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setSku(String sku) { this.sku = sku; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setCategory(Category category) { this.category = category; }
    public void setBrand(Brand brand) { this.brand = brand; }
    public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }
    public void setSellingPrice(BigDecimal sellingPrice) { this.sellingPrice = sellingPrice; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
    public void setMinimumStock(Integer minimumStock) { this.minimumStock = minimumStock; }
    public void setActive(boolean active) { this.active = active; }
    public void setSpecifications(List<ProductSpecification> specifications) { this.specifications = specifications; }
}
