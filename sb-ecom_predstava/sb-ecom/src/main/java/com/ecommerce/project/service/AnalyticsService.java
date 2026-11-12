package com.ecommerce.project.service;

import com.ecommerce.project.payload.AnalyticsResponse;
import com.ecommerce.project.payload.MonthlyRevenueDTO;
import com.ecommerce.project.payload.MonthlySoldTicketsDTO;
import com.ecommerce.project.payload.ShowOccupancyDTO;

import java.util.List;

public interface AnalyticsService {

    AnalyticsResponse getAnalyticsData();

    List<MonthlyRevenueDTO> getMonthlyRevenue();

    List<ShowOccupancyDTO> getShowOccupancy();

    List<MonthlySoldTicketsDTO> getMonthlySoldTickets();
}
