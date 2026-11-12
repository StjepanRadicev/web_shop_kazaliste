package com.ecommerce.project.service;

import com.ecommerce.project.model.PerformanceSeatStatus;
import com.ecommerce.project.model.PerformanceStatus;
import com.ecommerce.project.payload.AnalyticsResponse;
import com.ecommerce.project.payload.MonthlyRevenueDTO;
import com.ecommerce.project.payload.MonthlySoldTicketsDTO;
import com.ecommerce.project.payload.ShowOccupancyDTO;
import com.ecommerce.project.repositories.OrderItemRepository;
import com.ecommerce.project.repositories.OrderRepository;
import com.ecommerce.project.repositories.PerformanceRepository;
import com.ecommerce.project.repositories.PerformanceSeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

@Service
public class AnalyticsServiceImpl  implements AnalyticsService{

    @Autowired
    private PerformanceRepository performanceRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private PerformanceSeatRepository performanceSeatRepository;


    @Override
    public AnalyticsResponse getAnalyticsData() {
        AnalyticsResponse response = new AnalyticsResponse();
        Double totalRevenue = orderRepository.getTotalPaidRevenue();

        BigDecimal revenue = BigDecimal.valueOf(
                totalRevenue != null ? totalRevenue : 0.0
        ).setScale(2, RoundingMode.HALF_UP);

        long performanceCount = performanceRepository.count();

        long totalOrders = orderRepository.count();

        long totalSoldTickets = orderItemRepository.countSoldTickets();

        response.setTotalSoldTickets(String.valueOf(totalSoldTickets));


        response.setPerformanceCount(String.valueOf(performanceCount));
        response.setTotalOrders(String.valueOf(totalOrders));
        response.setTotalRevenue(String.valueOf(revenue));

        return response;
    }



    @Override
    public List<MonthlyRevenueDTO> getMonthlyRevenue() {

        List<Object[]> results = orderRepository.getMonthlyRevenue();

        return results.stream()
                .map(row -> new MonthlyRevenueDTO(
                        ((Number) row[0]).intValue(),
                        ((Number) row[1]).intValue(),
                        BigDecimal.valueOf(
                                ((Number) row[2]).doubleValue()
                        ).setScale(2, RoundingMode.HALF_UP)
                ))
                .toList();
    }



    @Override
    public List<ShowOccupancyDTO> getShowOccupancy() {

        List<Object[]> results = performanceSeatRepository.getShowOccupancy(
                PerformanceSeatStatus.SOLD,
                List.of(
                        PerformanceStatus.PUBLISHED,
                        PerformanceStatus.SOLD_OUT,
                        PerformanceStatus.FINISHED
                )
        );

        return results.stream().map(row -> {

                    Long showId = ((Number) row[0]).longValue();
                    String showName = (String) row[1];
                    Long performanceCount = ((Number) row[2]).longValue();
                    Long totalSeats = ((Number) row[3]).longValue();
                    Long soldSeats = ((Number) row[4]).longValue();

                    BigDecimal occupancyPercentage = totalSeats > 0
                            ? BigDecimal.valueOf(soldSeats)
                            .multiply(BigDecimal.valueOf(100))
                            .divide(BigDecimal.valueOf(totalSeats), 2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;

                    return new ShowOccupancyDTO(
                            showId,
                            showName,
                            performanceCount,
                            totalSeats,
                            soldSeats,
                            occupancyPercentage
                    );

                })
                .sorted(Comparator.comparing(
                        ShowOccupancyDTO::getOccupancyPercentage
                ).reversed())
                .limit(5)
                .toList();
    }



    @Override
    public List<MonthlySoldTicketsDTO> getMonthlySoldTickets() {

        List<Object[]> results = orderItemRepository.getMonthlySoldTickets();

        return results.stream()
                .map(row -> new MonthlySoldTicketsDTO(
                        ((Number) row[0]).intValue(),
                        ((Number) row[1]).intValue(),
                        ((Number) row[2]).longValue()
                ))
                .toList();
    }

}
