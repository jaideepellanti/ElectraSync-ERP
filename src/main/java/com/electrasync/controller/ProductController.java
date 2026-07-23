package com.electrasync.controller;

import com.electrasync.model.Brand;
import com.electrasync.model.Category;
import com.electrasync.model.Product;
import com.electrasync.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/manager/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "manager/products-list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", productService.getAllCategories());
        model.addAttribute("brands", productService.getAllBrands());
        return "manager/product-form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Product product = productService.getById(id).orElse(null);
        if (product == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Product not found.");
            return "redirect:/manager/products";
        }
        model.addAttribute("product", product);
        model.addAttribute("categories", productService.getAllCategories());
        model.addAttribute("brands", productService.getAllBrands());
        return "manager/product-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Product product,
                       @RequestParam(required = false) List<String> specNames,
                       @RequestParam(required = false) List<String> specValues,
                       RedirectAttributes redirectAttributes) {
        try {
            Product savedProduct = productService.saveProduct(product);
            productService.saveSpecifications(savedProduct.getId(), specNames, specValues);
            redirectAttributes.addFlashAttribute("successMessage", "Product saved successfully.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/manager/products";
    }

    // Called via JavaScript when the manager picks a category on the Add Product form,
    // so the spec fields can be pre-filled with suggestions relevant to that category
    // (e.g. RAM/Storage/Color for Mobiles, Screen Size/Resolution for TVs).
    @GetMapping("/spec-suggestions")
    @ResponseBody
    public List<String> getSpecSuggestions(@RequestParam Long categoryId) {
        return productService.getSuggestedSpecNames(categoryId);
    }

    @PostMapping("/deactivate/{id}")
    public String deactivate(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.deactivateProduct(id);
            redirectAttributes.addFlashAttribute("successMessage", "Product deactivated.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Could not deactivate product.");
        }
        return "redirect:/manager/products";
    }

    @GetMapping("/low-stock")
    public String lowStock(Model model) {
        model.addAttribute("products", productService.getLowStockProducts());
        return "manager/low-stock";
    }

    // ---------------- Categories ----------------

    @GetMapping("/categories")
    public String categories(Model model) {
        model.addAttribute("categories", productService.getAllCategories());
        model.addAttribute("category", new Category());
        return "manager/categories";
    }

    // Loads an existing category into the same form for editing
    @GetMapping("/categories/edit/{id}")
    public String editCategory(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Category category = productService.getCategoryById(id).orElse(null);
        if (category == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Category not found.");
            return "redirect:/manager/products/categories";
        }
        model.addAttribute("categories", productService.getAllCategories());
        model.addAttribute("category", category);
        return "manager/categories";
    }

    @PostMapping("/categories/save")
    public String saveCategory(@ModelAttribute Category category, RedirectAttributes redirectAttributes) {
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Category name is required.");
            return "redirect:/manager/products/categories";
        }
        try {
            productService.saveCategory(category);
            redirectAttributes.addFlashAttribute("successMessage", "Category '" + category.getName() + "' saved successfully.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/manager/products/categories";
    }

    // ---------------- Brands ----------------

    @GetMapping("/brands")
    public String brands(Model model) {
        model.addAttribute("brands", productService.getAllBrands());
        model.addAttribute("brand", new Brand());
        return "manager/brands";
    }

    // Loads an existing brand into the same form for editing
    @GetMapping("/brands/edit/{id}")
    public String editBrand(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Brand brand = productService.getBrandById(id).orElse(null);
        if (brand == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Brand not found.");
            return "redirect:/manager/products/brands";
        }
        model.addAttribute("brands", productService.getAllBrands());
        model.addAttribute("brand", brand);
        return "manager/brands";
    }

    @PostMapping("/brands/save")
    public String saveBrand(@ModelAttribute Brand brand, RedirectAttributes redirectAttributes) {
        if (brand.getName() == null || brand.getName().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Brand name is required.");
            return "redirect:/manager/products/brands";
        }
        try {
            productService.saveBrand(brand);
            redirectAttributes.addFlashAttribute("successMessage", "Brand '" + brand.getName() + "' saved successfully.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/manager/products/brands";
    }
}
