package com.ecommerce.project.service;

import com.ecommerce.project.payload.CartDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CartService {


    CartDTO addPerformanceToCart(Long performanceId, Integer quantity);

    List<CartDTO> getAllCarts();

    CartDTO getCart(String emailId, Long cartId);

    @Transactional
    CartDTO updatePerformanceQuantityInCart(Long performanceId, Integer quantity);

    String deletePerformanceFromCart(Long cartId, Long performanceId);

    void updatePerformanceInCarts(Long cartId, Long performanceId);
}
