package com.electrasync.service;

import com.electrasync.model.Product;
import com.electrasync.model.PurchaseOrder;
import com.electrasync.model.PurchaseOrderItem;
import com.electrasync.repository.ProductRepository;
import com.electrasync.repository.PurchaseOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final ProductRepository productRepository;

    public PurchaseOrderService(PurchaseOrderRepository purchaseOrderRepository,
                                ProductRepository productRepository) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.productRepository = productRepository;
    }

    public List<PurchaseOrder> getAll() {
        return purchaseOrderRepository.findAll();
    }

    public Optional<PurchaseOrder> getById(Long id) {
        return purchaseOrderRepository.findById(id);
    }

    public long countPendingOrders() {
        return purchaseOrderRepository.countPending();
    }

    // Generates a unique PO number like PO-2026-00001
    // Note: count() + 1 is fine for single-user systems. For high concurrency, use a sequence.
    public String generatePoNumber() {
        long count = purchaseOrderRepository.count() + 1;
        int year = LocalDate.now().getYear();
        return String.format("PO-%d-%05d", year, count);
    }

    // Creates a new purchase order and calculates the total from line items
    @Transactional
    public PurchaseOrder createOrder(PurchaseOrder po) {
        BigDecimal total = BigDecimal.ZERO;

        if (po.getItems() != null) {
            for (PurchaseOrderItem item : po.getItems()) {
                item.setPurchaseOrder(po);
                BigDecimal lineTotal = item.getUnitCostPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                item.setLineTotal(lineTotal);
                total = total.add(lineTotal);
            }
        }

        po.setTotalAmount(total);
        return purchaseOrderRepository.save(po);
    }

    // Receives goods from vendor - updates stock quantity for each product in the order
    @Transactional
    public PurchaseOrder receiveOrder(Long poId) {
        PurchaseOrder po = purchaseOrderRepository.findById(poId)
                .orElseThrow(() -> new IllegalArgumentException("Purchase Order not found."));

        if (po.getStatus() == PurchaseOrder.POStatus.RECEIVED) {
            throw new IllegalStateException("This order has already been received.");
        }

        for (PurchaseOrderItem item : po.getItems()) {
            Product product = item.getProduct();
            // Increase stock by the quantity received from vendor
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            // Update cost price to the latest purchase price
            product.setCostPrice(item.getUnitCostPrice());
            productRepository.save(product);
        }

        po.setStatus(PurchaseOrder.POStatus.RECEIVED);
        po.setDeliveryDate(LocalDate.now());
        return purchaseOrderRepository.save(po);
    }

    // Records a payment made to the vendor. Cannot pay more than the amount due.
    @Transactional
    public PurchaseOrder recordPayment(Long poId, BigDecimal amount) {
        PurchaseOrder po = purchaseOrderRepository.findById(poId)
                .orElseThrow(() -> new IllegalArgumentException("Purchase Order not found."));

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero.");
        }

        BigDecimal amountDue = po.getAmountDue();
        if (amount.compareTo(amountDue) > 0) {
            throw new IllegalArgumentException("Payment amount ₹" + amount + " exceeds the amount due ₹" + amountDue + ".");
        }

        BigDecimal currentPaid = po.getAmountPaid() == null ? BigDecimal.ZERO : po.getAmountPaid();
        po.setAmountPaid(currentPaid.add(amount));
        return purchaseOrderRepository.save(po);
    }

    @Transactional
    public void cancelOrder(Long poId) {
        PurchaseOrder po = purchaseOrderRepository.findById(poId)
                .orElseThrow(() -> new IllegalArgumentException("Purchase Order not found."));

        if (po.getStatus() == PurchaseOrder.POStatus.RECEIVED) {
            throw new IllegalStateException("Cannot cancel an order that has already been received.");
        }

        po.setStatus(PurchaseOrder.POStatus.CANCELLED);
        purchaseOrderRepository.save(po);
    }
}
