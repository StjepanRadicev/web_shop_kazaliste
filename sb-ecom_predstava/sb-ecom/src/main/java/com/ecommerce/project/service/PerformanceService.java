package com.ecommerce.project.service;

import com.ecommerce.project.payload.PerformanceDTO;
import com.ecommerce.project.payload.PerformanceResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

public interface PerformanceService {

    PerformanceResponse getAllPerformances();

    PerformanceResponse searchByCategory(Long categoryId);

    PerformanceResponse getPerformancesByAll(Integer pageNumber, Integer pageSize, String category, String showId, String sortBy, String sortDir, String name);

    PerformanceDTO updatePerformance(PerformanceDTO performanceDTO, Long performanceId);

    PerformanceDTO addPerformance(Long showId, Long hallId, @Valid PerformanceDTO performanceDTO);

    PerformanceDTO patchedUpdatePerformance(Long performanceId, Map<String, Object> patchPayLoad);

    PerformanceDTO deletePerformance(Long performanceId);

    PerformanceDTO updatePerformanceImage(Long performanceId, MultipartFile image) throws IOException;

    PerformanceResponse getAllPerformances(@Min(0) Integer pageNumber, @Min(1) @Max(100) Integer pageSize, String sortBy, String sortDir);


}
