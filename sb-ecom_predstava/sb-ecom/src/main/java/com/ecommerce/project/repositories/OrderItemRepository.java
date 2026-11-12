package com.ecommerce.project.repositories;

import com.ecommerce.project.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {



    @Query("""
    SELECT COUNT(oi)
    FROM OrderItem oi
    WHERE oi.order.orderStatus = 'PAID'
    """)
    Long countSoldTickets();



    @Query("""
    SELECT
        YEAR(oi.order.orderDate),
        MONTH(oi.order.orderDate),
        COUNT(oi)
    FROM OrderItem oi
    WHERE oi.order.orderStatus = 'PAID'
    GROUP BY
        YEAR(oi.order.orderDate),
        MONTH(oi.order.orderDate)
    ORDER BY
        YEAR(oi.order.orderDate),
        MONTH(oi.order.orderDate)
    """)
    List<Object[]> getMonthlySoldTickets();

    boolean existsByPerformanceSeat_Performance_PerformanceId(
            Long performanceId
    );


//    @Query("""
//            SELECT COALESCE(SUM(oi.quantity), 0)
//            FROM OrderItem oi
//            WHERE oi.performance.performanceId = :performanceId
//            """)
//    Integer findTotalOrderedByPerformanceId(@Param("performanceId") Long performanceId);

}
