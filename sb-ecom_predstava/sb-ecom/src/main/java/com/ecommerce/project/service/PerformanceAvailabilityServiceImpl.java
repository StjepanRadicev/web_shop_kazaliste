package com.ecommerce.project.service;

import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.Performance;
import com.ecommerce.project.repositories.CartItemRepository;
import com.ecommerce.project.repositories.OrderItemRepository;
import com.ecommerce.project.repositories.PerformanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PerformanceAvailabilityServiceImpl implements PerformanceAvailabilityService{

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private PerformanceRepository performanceRepository;


    @Override
    public int getAvailability(Long performanceId) {

        int reservedPerformances = cartItemRepository.findTotalReservedByPerformanceId(performanceId);

        int orderedPerformances = orderItemRepository.findTotalOrderedByPerformanceId(performanceId);

        Performance performance = performanceRepository.findById(performanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Performance", "performanceId", performanceId));

        return performance.getQuantity() - reservedPerformances - orderedPerformances;
    }
}
