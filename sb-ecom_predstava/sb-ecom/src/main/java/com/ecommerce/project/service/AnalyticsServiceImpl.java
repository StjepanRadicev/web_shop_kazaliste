package com.ecommerce.project.service;

import com.ecommerce.project.payload.AnalyticsResponse;
import com.ecommerce.project.repositories.OrderRepository;
import com.ecommerce.project.repositories.PerformanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class AnalyticsServiceImpl  implements AnalyticsService{

    @Autowired
    private PerformanceRepository performanceRepository;

    @Autowired
    private OrderRepository orderRepository;


    @Override
    public AnalyticsResponse getAnalyticsData() {
        AnalyticsResponse response = new AnalyticsResponse();
        Double totalRevenue = orderRepository.getTotalRevenue();

        BigDecimal revenue = BigDecimal.valueOf(
                totalRevenue != null ? totalRevenue : 0.0
        ).setScale(2, RoundingMode.HALF_UP);

        long performanceCount = performanceRepository.count();

        long totalOrders = orderRepository.count();


        response.setPerformanceCount(String.valueOf(performanceCount));
        response.setTotalOrders(String.valueOf(totalOrders));
        response.setTotalRevenue(String.valueOf(revenue));

        return response;
    }
}
