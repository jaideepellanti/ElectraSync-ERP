package com.electrasync.service;

import com.electrasync.model.Employee;
import com.electrasync.model.Product;
import com.electrasync.model.Sale;
import com.electrasync.model.SaleItem;
import com.electrasync.repository.CustomerRepository;
import com.electrasync.repository.EmployeeRepository;
import com.electrasync.repository.ProductRepository;
import com.electrasync.repository.PurchaseOrderRepository;
import com.electrasync.repository.SalaryPaymentRepository;
import com.electrasync.repository.SaleRepository;
import com.electrasync.repository.VendorRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// This service gathers data from multiple repositories to build the dashboard summary.
// Keeping it separate from other services keeps the dashboard logic in one place.
@Service
public class DashboardService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SalaryPaymentRepository salaryPaymentRepository;
    private final EmployeeRepository employeeRepository;
    private final CustomerRepository customerRepository;
    private final VendorRepository vendorRepository;

    public DashboardService(SaleRepository saleRepository,
                            ProductRepository productRepository,
                            PurchaseOrderRepository purchaseOrderRepository,
                            SalaryPaymentRepository salaryPaymentRepository,
                            EmployeeRepository employeeRepository,
                            CustomerRepository customerRepository,
                            VendorRepository vendorRepository) {
        this.saleRepository = saleRepository;
        this.productRepository = productRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.salaryPaymentRepository = salaryPaymentRepository;
        this.employeeRepository = employeeRepository;
        this.customerRepository = customerRepository;
        this.vendorRepository = vendorRepository;
    }

    public Map<String, Object> getOwnerDashboardData() {
        Map<String, Object> data = new HashMap<>();

        LocalDateTime now = LocalDateTime.now();
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();

        // Sales figures
        data.put("todayRevenue", nz(saleRepository.todayRevenue()));
        data.put("todaySalesCount", saleRepository.countTodaySales());
        data.put("monthRevenue", nz(saleRepository.sumRevenueBetween(startOfMonth, now)));
        data.put("monthProfit", calculateMonthProfit(startOfMonth, now));

        // Inventory figures
        List<Product> lowStockProducts = productRepository.findLowStockProducts();
        data.put("lowStockCount", lowStockProducts.size());
        data.put("lowStockProducts", lowStockProducts);
        data.put("totalProducts", productRepository.findByActiveTrue().size());

        // Purchasing figures
        data.put("pendingPOs", purchaseOrderRepository.countPending());
        data.put("activeVendors", vendorRepository.findByActiveTrue().size());

        // People figures
        data.put("activeEmployees", employeeRepository.findByStatus(Employee.EmployeeStatus.ACTIVE).size());
        data.put("totalCustomers", customerRepository.findAll().size());

        BigDecimal monthSalary = salaryPaymentRepository.totalSalaryForMonth(
                currentMonth.getMonthValue(), currentMonth.getYear());
        data.put("monthSalaryPaid", nz(monthSalary));

        // Recent activity - last 5 sales for the dashboard feed
        data.put("recentSales", getRecentSales());

        return data;
    }

    // Calculates profit for the current month using the cost/price snapshot on each sale item
    private BigDecimal calculateMonthProfit(LocalDateTime start, LocalDateTime end) {
        List<Sale> monthSales = saleRepository.findSalesBetween(start, end);
        BigDecimal totalProfit = BigDecimal.ZERO;

        for (Sale sale : monthSales) {
            if (sale.getStatus() != Sale.SaleStatus.COMPLETED) continue;
            for (SaleItem item : sale.getSaleItems()) {
                totalProfit = totalProfit.add(item.getLineProfit());
            }
        }
        return totalProfit;
    }

    // Returns the 5 most recent sales for the "Recent Activity" section on the dashboard
    private List<Sale> getRecentSales() {
        return saleRepository.findAll(PageRequest.of(0, 5,
                org.springframework.data.domain.Sort.by("saleDate").descending())).getContent();
    }

    private BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
