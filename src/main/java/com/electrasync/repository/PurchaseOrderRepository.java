package com.electrasync.repository;

import com.electrasync.model.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    // Counts orders still waiting to be received — shown on the dashboard
    @Query("SELECT COUNT(po) FROM PurchaseOrder po WHERE po.status = 'PENDING'")
    long countPending();
}
