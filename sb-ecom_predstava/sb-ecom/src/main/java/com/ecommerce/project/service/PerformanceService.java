package com.ecommerce.project.service;

import com.ecommerce.project.payload.PerformanceDTO;
import com.ecommerce.project.payload.PerformanceResponse;
import jakarta.validation.Valid;

import java.util.Map;

public interface PerformanceService {

    PerformanceResponse getAllPerformances();

    PerformanceResponse searchPerformanceByKeyword(String keyword);

    PerformanceResponse searchByCategory(Long categoryId);

    PerformanceResponse getPerformancesByAll(Integer pageNumber, Integer pageSize, Long categoryId, Long showId, String sortBy, String sortDir);

    PerformanceDTO updatePerformance(PerformanceDTO performanceDTO, Long performanceId);

    PerformanceDTO addPerformance(Long showId, Long hallId, @Valid PerformanceDTO performanceDTO);

    PerformanceDTO patchedUpdatePerformance(Long performanceId, Map<String, Object> patchPayLoad);

    PerformanceDTO deletePerformance(Long performanceId);
}
