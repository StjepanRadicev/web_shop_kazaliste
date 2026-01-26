package com.ecommerce.project.service;

import com.ecommerce.project.payload.SeatDTO;

import java.util.List;

public interface PerformanceSeatService {
    List<SeatDTO> getSeat(Long performanceId);
}
