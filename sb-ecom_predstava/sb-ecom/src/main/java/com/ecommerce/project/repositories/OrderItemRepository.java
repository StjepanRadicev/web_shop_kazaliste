package com.ecommerce.project.repositories;

import com.ecommerce.project.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("""
            SELECT COALESCE(SUM(oi.quantity), 0)
            FROM OrderItem oi
            WHERE oi.performance.performanceId = :performanceId
            """)
    Integer findTotalOrderedByPerformanceId(@Param("performanceId") Long performanceId);
}
