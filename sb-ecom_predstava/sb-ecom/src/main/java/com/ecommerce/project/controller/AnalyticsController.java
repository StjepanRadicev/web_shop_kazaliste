package com.ecommerce.project.controller;

import com.ecommerce.project.payload.AnalyticsResponse;
import com.ecommerce.project.payload.MonthlyRevenueDTO;
import com.ecommerce.project.payload.MonthlySoldTicketsDTO;
import com.ecommerce.project.payload.ShowOccupancyDTO;
import com.ecommerce.project.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/admin/app/analytics")
    public ResponseEntity<AnalyticsResponse> getAnalytics() {
        AnalyticsResponse response = analyticsService.getAnalyticsData();

        return new ResponseEntity<AnalyticsResponse>(response, HttpStatus.OK);
    }


    @GetMapping("/admin/app/analytics/revenue")
    public ResponseEntity<List<MonthlyRevenueDTO>> getMonthlyRevenue() {

        List<MonthlyRevenueDTO> monthlyRevenue =
                analyticsService.getMonthlyRevenue();

        return ResponseEntity.ok(monthlyRevenue);
    }



    @GetMapping("/admin/app/analytics/occupancy")
    public ResponseEntity<List<ShowOccupancyDTO>> getShowOccupancy() {
        return ResponseEntity.ok(analyticsService.getShowOccupancy());
    }



    @GetMapping("/admin/app/analytics/tickets")
    public ResponseEntity<List<MonthlySoldTicketsDTO>> getMonthlySoldTickets() {
        return ResponseEntity.ok(analyticsService.getMonthlySoldTickets());
    }

}
