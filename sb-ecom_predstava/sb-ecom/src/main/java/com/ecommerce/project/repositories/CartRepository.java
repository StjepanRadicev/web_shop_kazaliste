package com.ecommerce.project.repositories;

import com.ecommerce.project.model.Cart;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    @Query("SELECT c FROM Cart c WHERE c.user.email = ?1")
    Cart findCartByEmail(String email);

    @Query("SELECT c FROM Cart c WHERE c.user.email = ?1 AND c.id = ?2")
    Cart findCartByEmailAndCartId(String emailId, Long cartId);

//    @Query("SELECT c FROM Cart c JOIN FETCH c.cartItems ci JOIN FETCH ci.performance p WHERE p.id = ?1")
//    List<Cart> findCartsByPerformanceId(Long performanceId);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Cart c WHERE c.cartId = :cartId")
    Optional<Cart> findByIdForUpdate(@Param("cartId") Long cartId);
}
