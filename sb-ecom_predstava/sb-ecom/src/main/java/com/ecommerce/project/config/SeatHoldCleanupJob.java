package com.ecommerce.project.config;

import com.ecommerce.project.repositories.CartItemRepository;
import com.ecommerce.project.repositories.PerformanceSeatRepository;
import com.ecommerce.project.service.CartService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class SeatHoldCleanupJob {

    @Autowired
    private CartService cartService;


    @Scheduled(fixedDelay = 60_000) // 1 min
    @Transactional
    public void cleanup() {
        cartService.cleanupExpiredHeldItems();

    }
}
