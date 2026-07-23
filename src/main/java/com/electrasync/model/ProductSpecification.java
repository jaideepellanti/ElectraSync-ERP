package com.electrasync.model;

import jakarta.persistence.*;

// Stores category-specific specifications for a product, as simple key-value pairs.
// Example: for a mobile phone -> ("RAM", "8GB"), ("Storage", "128GB"), ("Color", "Black")
// Example: for a TV          -> ("Screen Size", "55 inch"), ("Resolution", "4K UHD")
//
// This keeps the Product entity itself simple (no need for a different table per category)
// while still letting each product carry whatever attributes make sense for its category.
@Entity
@Table(name = "product_specifications")
public class ProductSpecification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // e.g. "RAM", "Storage", "Color", "Screen Size"
    @Column(nullable = false, length = 50)
    private String specName;

    // e.g. "8GB", "128GB", "Black", "55 inch"
    @Column(nullable = false, length = 100)
    private String specValue;

    // Getters
    public Long getId() { return id; }
    public Product getProduct() { return product; }
    public String getSpecName() { return specName; }
    public String getSpecValue() { return specValue; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setProduct(Product product) { this.product = product; }
    public void setSpecName(String specName) { this.specName = specName; }
    public void setSpecValue(String specValue) { this.specValue = specValue; }
}
