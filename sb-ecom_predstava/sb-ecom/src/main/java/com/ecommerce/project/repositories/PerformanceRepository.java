package com.ecommerce.project.repositories;


import com.ecommerce.project.model.Performance;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PerformanceRepository extends JpaRepository<Performance, Long>, JpaSpecificationExecutor<Performance> {

    List<Performance> findByShow_Category_CategoryId(Long categoryId);

    List<Performance> findByShow_Category_CategoryNameOrderByPriceAsc(String name);

    List<Performance> findByPerformanceNameContainingIgnoreCase(String keyword);

    Performance findByPerformanceNameIgnoreCase(String performanceName);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000"))
    @Query("SELECT p FROM Performance p WHERE p.performanceId = :id")
    Optional<Performance> findByIdForUpdate( Long id);
//@Param("id")
}
