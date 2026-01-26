package com.ecommerce.project.repositories;

import com.ecommerce.project.config.ExpiredHeldItem;
import com.ecommerce.project.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {



    @Query("""
  select ci.cart.id as cartId,
         ci.performanceSeat.performanceSeatId as performanceSeatId
  from CartItem ci
  where ci.performanceSeat.status = com.ecommerce.project.model.PerformanceSeatStatus.HELD
    and ci.performanceSeat.heldUntil < :now
    and ci.performanceSeat.heldByCartId = ci.cart.id
  """)
    List<ExpiredHeldItem> findExpiredHeldItems(@Param("now") LocalDateTime now);


    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = ?1 AND ci.performanceSeat.id = ?2")
    CartItem findCartItemByPerformanceSeatIdAndCartId(Long cartId, Long performanceSeatId);


    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = ?1 AND ci.performanceSeat.id = ?2")
    void deleteCartItemByPerformanceSeatIdAndCartId(Long cartId, Long performanceSeatId);
}
