package com.ecommerce.project.service;

import com.ecommerce.project.model.Performance;

public interface PerformanceAvailabilityService {

    int getAvailability(Performance performance, Long performanceId);
}
