package com.ecommerce.project.repositories;


import com.ecommerce.project.model.Performance;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PerformanceRepository extends JpaRepository<Performance, Long>, JpaSpecificationExecutor<Performance> {

    List<Performance> findByShow_Category_CategoryId(Long categoryId);

    List<Performance> findByShow_Category_CategoryNameOrderByPriceAsc(String name);

    List<Performance> findByPerformanceNameContainingIgnoreCase(String keyword);

    Performance findByPerformanceNameIgnoreCase(String performanceName);

    @Query(value = """
            SELECT * FROM performance WHERE local_date_time > NOW()""", nativeQuery = true)
    Performance findActivePerformance();

    @Modifying
    @Query("""
        update Performance p
        set p.status = 'FINISHED'
        where p.status = 'PUBLISHED'
          and p.localDateTime < :now
    """)
    int markFinished(@Param("now") LocalDateTime now);

}
