package com.electrasync.service;

import com.electrasync.model.Product;
import com.electrasync.model.Sale;
import com.electrasync.model.SaleItem;
import com.electrasync.repository.ProductRepository;
import com.electrasync.repository.SaleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SaleService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;

    public SaleService(SaleRepository saleRepository, ProductRepository productRepository) {
        this.saleRepository = saleRepository;
        this.productRepository = productRepository;
    }

    public List<Sale> getAllSales() {
        return saleRepository.findAll();
    }

    public Optional<Sale> getById(Long id) {
        return saleRepository.findById(id);
    }

    public List<Sale> getSalesBetween(LocalDateTime from, LocalDateTime to) {
        return saleRepository.findSalesBetween(from, to);
    }

    public BigDecimal getTodayRevenue() {
        BigDecimal revenue = saleRepository.todayRevenue();
        return revenue == null ? BigDecimal.ZERO : revenue;
    }

    public long getTodaySalesCount() {
        return saleRepository.countTodaySales();
    }

    public BigDecimal getRevenueBetween(LocalDateTime from, LocalDateTime to) {
        BigDecimal revenue = saleRepository.sumRevenueBetween(from, to);
        return revenue == null ? BigDecimal.ZERO : revenue;
    }

    // Generates a unique invoice number like INV-2026-00001
    // Note: count() + 1 works for single-user usage. Add a DB sequence for multi-cashier production use.
    private String generateInvoiceNumber() {
        long count = saleRepository.count() + 1;
        int year = LocalDate.now().getYear();
        return String.format("INV-%d-%05d", year, count);
    }

    // Main POS checkout method:
    // 1. Validates stock is available for each product
    // 2. Deducts stock immediately
    // 3. Snapshots unit price and cost at time of sale (important for historical accuracy)
    // 4. Applies discount and calculates final total
    @Transactional
    public Sale processCheckout(Sale sale) {
        if (sale.getSaleItems() == null || sale.getSaleItems().isEmpty()) {
            throw new IllegalArgumentException("Cannot create a sale with no items.");
        }

        BigDecimal subTotal = BigDecimal.ZERO;

        for (SaleItem item : sale.getSaleItems()) {
            Product product = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found."));

            // Check if enough stock is available before processing
            if (product.getStockQuantity() < item.getQuantity()) {
                throw new IllegalStateException("Not enough stock for '" + product.getName() + "'. "
                        + "Available: " + product.getStockQuantity() + ", Requested: " + item.getQuantity());
            }

            // Snapshot prices so the invoice is accurate even if product prices change later
            item.setUnitPrice(product.getSellingPrice());
            item.setUnitCost(product.getCostPrice());
            item.setLineTotal(product.getSellingPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            item.setSale(sale);

            subTotal = subTotal.add(item.getLineTotal());

            // Deduct stock right away
            product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
            productRepository.save(product);
        }

        // Apply discount
        BigDecimal discountPercent = sale.getDiscountPercent() == null ? BigDecimal.ZERO : sale.getDiscountPercent();
        BigDecimal discountAmount = subTotal.multiply(discountPercent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        sale.setSubTotal(subTotal);
        sale.setDiscountAmount(discountAmount);
        sale.setTotalAmount(subTotal.subtract(discountAmount));
        sale.setInvoiceNumber(generateInvoiceNumber());
        sale.setSaleDate(LocalDateTime.now());
        sale.setStatus(Sale.SaleStatus.COMPLETED);

        return saleRepository.save(sale);
    }

    // Processes a return — restores stock and marks sale as returned
    @Transactional
    public void returnSale(Long saleId) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new IllegalArgumentException("Sale not found."));

        if (sale.getStatus() == Sale.SaleStatus.RETURNED) {
            throw new IllegalStateException("This sale has already been returned.");
        }

        for (SaleItem item : sale.getSaleItems()) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);
        }

        sale.setStatus(Sale.SaleStatus.RETURNED);
        saleRepository.save(sale);
    }

    // Calculates total profit for a date range using snapshotted cost prices on sale items
    public BigDecimal getProfitBetween(LocalDateTime from, LocalDateTime to) {
        List<Sale> sales = saleRepository.findSalesBetween(from, to);
        BigDecimal totalProfit = BigDecimal.ZERO;

        for (Sale sale : sales) {
            if (sale.getStatus() != Sale.SaleStatus.COMPLETED) continue;
            for (SaleItem item : sale.getSaleItems()) {
                totalProfit = totalProfit.add(item.getLineProfit());
            }
        }

        return totalProfit;
    }
}
