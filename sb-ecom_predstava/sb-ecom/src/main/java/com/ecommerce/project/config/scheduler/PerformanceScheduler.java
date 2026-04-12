package com.ecommerce.project.config.scheduler;

import com.ecommerce.project.repositories.PerformanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PerformanceScheduler {

    private final PerformanceRepository performanceRepository;

    public PerformanceScheduler(PerformanceRepository performanceRepository) {
        this.performanceRepository = performanceRepository;
    }

    @Scheduled(fixedDelay = 60000) // svake minute
    @Transactional
    public void updateFinishedPerformances() {
        int updated = performanceRepository.markFinished(LocalDateTime.now());
        System.out.println("Marked as FINISHED: " + updated);
    }
}
