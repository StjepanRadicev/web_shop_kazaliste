package com.ecommerce.project.config.scheduler;

import com.ecommerce.project.service.CartService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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
