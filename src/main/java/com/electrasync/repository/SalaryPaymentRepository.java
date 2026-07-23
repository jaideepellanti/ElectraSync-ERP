package com.electrasync.repository;

import com.electrasync.model.SalaryPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface SalaryPaymentRepository extends JpaRepository<SalaryPayment, Long> {

    List<SalaryPayment> findByEmployeeId(Long employeeId);

    List<SalaryPayment> findByPayMonthAndPayYear(int month, int year);

    // Used to prevent paying salary twice for the same month
    Optional<SalaryPayment> findByEmployeeIdAndPayMonthAndPayYear(Long employeeId, int month, int year);

    @Query("SELECT COALESCE(SUM(sp.netSalary), 0) FROM SalaryPayment sp WHERE sp.payMonth = :month AND sp.payYear = :year")
    BigDecimal totalSalaryForMonth(@Param("month") int month, @Param("year") int year);
}
