package com.ecommerce.project.repositories;

import com.ecommerce.project.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = ?1 AND ci.performance.id = ?2")
    CartItem findCartItemByPerformanceIdAndCartId(Long cartId, Long performanceId);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = ?1 AND ci.performance.id = ?2")
    void deleteCartItemByPerformanceIdAndCartId(Long cartId, Long performanceId);

    @Query("SELECT ci.performance.performanceId, SUM(ci.quantity) " +
            "FROM CartItem ci GROUP BY ci.performance.performanceId")
    List<Object[]> findTotalQuantities();

    @Query("""
            SELECT COALESCE(SUM(ci.quantity), 0)
            FROM CartItem ci
            WHERE ci.performance.performanceId = :performanceId
            """)
    Integer findTotalReservedByPerformanceId(@Param("performanceId") Long performanceId);
}
