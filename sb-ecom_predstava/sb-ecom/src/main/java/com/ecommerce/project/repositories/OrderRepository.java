package com.ecommerce.project.repositories;

import com.ecommerce.project.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByPayment_PgPaymentId(String pgPaymentId);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o")
    Double getTotalRevenue();


    @Query("""
    SELECT COALESCE(SUM(o.totalAmount), 0)
    FROM Order o
    WHERE o.orderStatus = 'PAID'
    """)
    Double getTotalPaidRevenue();



    @Query("""
    SELECT YEAR(o.orderDate),
           MONTH(o.orderDate),
           SUM(o.totalAmount)
    FROM Order o
    WHERE o.orderStatus = 'PAID'
    GROUP BY YEAR(o.orderDate), MONTH(o.orderDate)
    ORDER BY YEAR(o.orderDate), MONTH(o.orderDate)
    """)
    List<Object[]> getMonthlyRevenue();


    boolean existsByAddress_AddressId(Long addressId);

    Page<Order> findByEmailIgnoreCase(String email, Pageable pageable);

    boolean existsByEmailIgnoreCase(String email);



}
