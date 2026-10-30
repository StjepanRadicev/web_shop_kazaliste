package com.ecommerce.project.repositories;

import com.ecommerce.project.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByPayment_PgPaymentId(String pgPaymentId);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o")
    Double getTotalRevenue();

    boolean existsByAddress_AddressId(Long addressId);
}
