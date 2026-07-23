package com.electrasync.service;

import com.electrasync.model.Brand;
import com.electrasync.model.Category;
import com.electrasync.model.Product;
import com.electrasync.model.ProductSpecification;
import com.electrasync.repository.BrandRepository;
import com.electrasync.repository.CategoryRepository;
import com.electrasync.repository.ProductRepository;
import com.electrasync.repository.ProductSpecificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductSpecificationRepository specificationRepository;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          BrandRepository brandRepository,
                          ProductSpecificationRepository specificationRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.specificationRepository = specificationRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getAllActiveProducts() {
        return productRepository.findByActiveTrue();
    }

    public Optional<Product> getById(Long id) {
        return productRepository.findById(id);
    }

    public List<Product> searchByName(String keyword) {
        return productRepository.searchByName(keyword);
    }

    public List<Product> getLowStockProducts() {
        return productRepository.findLowStockProducts();
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    public List<Brand> getAllBrands() {
        return brandRepository.findAll();
    }

    public Optional<Brand> getBrandById(Long id) {
        return brandRepository.findById(id);
    }

    // Returns suggested specification field names based on the category
    // (e.g. RAM/Storage/Color for a mobile category). Used by the Add Product form
    // to pre-fill helpful spec name suggestions.
    public List<String> getSuggestedSpecNames(Long categoryId) {
        Category category = categoryRepository.findById(categoryId).orElse(null);
        if (category == null) {
            return List.of("Color", "Model", "Warranty");
        }
        return CategorySpecSuggestions.getSuggestedSpecNames(category.getName());
    }

    // Validates and saves a product:
    // - SKU must be unique
    // - Selling price cannot be less than cost price (would mean selling at a loss)
    // - Stock quantity cannot be negative
    // - When editing an existing product, stock quantity is preserved (it's a disabled
    //   field on the edit form) and can only change via Purchase Orders or Sales.
    @Transactional
    public Product saveProduct(Product product) {
        Optional<Product> existingProduct = productRepository.findBySku(product.getSku().trim());
        if (existingProduct.isPresent() && !existingProduct.get().getId().equals(product.getId())) {
            throw new IllegalArgumentException("A product with SKU '" + product.getSku() + "' already exists.");
        }

        if (product.getSellingPrice().compareTo(product.getCostPrice()) < 0) {
            throw new IllegalArgumentException("Selling price cannot be less than cost price.");
        }

        // If this is an existing product, keep its current stock quantity.
        // The stock field is disabled on the edit form, so the browser won't send it,
        // which would otherwise overwrite stock with null/0.
        //
        // For a brand NEW product, stock is always forced to 0 - there is no "Opening Stock"
        // field anymore. This is intentional: the only way stock can ever increase is through
        // a Purchase Order that is marked as Received from a vendor. This guarantees every unit
        // of stock in the system is traceable to a specific vendor and a specific price paid,
        // and the owner always knows exactly how much is owed to each vendor.
        if (product.getId() != null) {
            Product currentProduct = productRepository.findById(product.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found."));
            product.setStockQuantity(currentProduct.getStockQuantity());
        } else {
            product.setStockQuantity(0);
        }

        if (product.getStockQuantity() < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative.");
        }

        // BUG FIX: minimumStock had a placeholder="5" in the HTML form, but a placeholder
        // is only hint text - if the field is left blank, the browser submits an empty value
        // and this binds as null. That silently broke the low-stock query (stockQuantity <= null
        // is always false in SQL), so no product ever showed up as low-stock no matter its quantity.
        // Defaulting it here guarantees it's never null.
        if (product.getMinimumStock() == null) {
            product.setMinimumStock(5);
        }
        if (product.getMinimumStock() < 0) {
            throw new IllegalArgumentException("Minimum stock cannot be negative.");
        }

        product.setSku(product.getSku().trim().toUpperCase());
        return productRepository.save(product);
    }

    // Saves the specification (RAM, Storage, Color, etc.) rows for a product.
    // Called separately from saveProduct() because the specs come in as two parallel
    // arrays from the form (spec names and spec values), not as part of the Product object.
    //
    // Replaces all existing specs for this product with the new list - simplest way to
    // keep this in sync without tracking individual row updates.
    @Transactional
    public void saveSpecifications(Long productId, List<String> specNames, List<String> specValues) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found."));

        // Remove old specifications for this product first
        specificationRepository.deleteByProductId(productId);

        if (specNames == null || specValues == null) {
            return;
        }

        List<ProductSpecification> newSpecs = new ArrayList<>();
        for (int i = 0; i < specNames.size(); i++) {
            String name = specNames.get(i) == null ? "" : specNames.get(i).trim();
            String value = i < specValues.size() && specValues.get(i) != null ? specValues.get(i).trim() : "";

            // Skip empty rows - the form may submit blank rows if the manager didn't fill them all
            if (name.isEmpty() || value.isEmpty()) {
                continue;
            }

            ProductSpecification spec = new ProductSpecification();
            spec.setProduct(product);
            spec.setSpecName(name);
            spec.setSpecValue(value);
            newSpecs.add(spec);
        }

        specificationRepository.saveAll(newSpecs);
    }

    // Soft delete - marks product as inactive instead of removing it from the database.
    // This keeps historical sales/purchase records intact.
    @Transactional
    public void deactivateProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));
        product.setActive(false);
        productRepository.save(product);
    }

    // Saves a category. Throws exception if a category with the same name already exists.
    @Transactional
    public Category saveCategory(Category category) {
        Optional<Category> existing = categoryRepository.findByNameIgnoreCase(category.getName().trim());
        if (existing.isPresent() && !existing.get().getId().equals(category.getId())) {
            throw new IllegalArgumentException("Category '" + category.getName() + "' already exists.");
        }
        category.setName(category.getName().trim());
        return categoryRepository.save(category);
    }

    // Saves a brand. Throws exception if a brand with the same name already exists.
    @Transactional
    public Brand saveBrand(Brand brand) {
        Optional<Brand> existing = brandRepository.findByNameIgnoreCase(brand.getName().trim());
        if (existing.isPresent() && !existing.get().getId().equals(brand.getId())) {
            throw new IllegalArgumentException("Brand '" + brand.getName() + "' already exists.");
        }
        brand.setName(brand.getName().trim());
        return brandRepository.save(brand);
    }
}
