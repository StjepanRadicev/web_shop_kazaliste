package com.ecommerce.project.repositories;


import com.ecommerce.project.model.Performance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PerformanceRepository extends JpaRepository<Performance, Long>, JpaSpecificationExecutor<Performance> {

    List<Performance> findByShow_Category_CategoryId(Long categoryId);

    List<Performance> findByShow_Category_CategoryNameOrderByPriceAsc(String name);

    List<Performance> findByPerformanceNameContainingIgnoreCase(String keyword);

    Performance findByPerformanceNameIgnoreCase(String performanceName);
}
