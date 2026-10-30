package com.ecommerce.project.repositories;


import com.ecommerce.project.model.Performance;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import jakarta.validation.constraints.FutureOrPresent;
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

    //List<Performance> findByPerformanceNameContainingIgnoreCase(String keyword);

    //Performance findByPerformanceNameIgnoreCase(String performanceName);

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

    @Query("""
        SELECT p 
        FROM Performance p 
        WHERE p.localDateTime BETWEEN :start AND :end
        """)
    List<Performance> findByHourRange(LocalDateTime start, LocalDateTime end);

    @Query(value = """
        SELECT p.*
        FROM performance p
        JOIN shows s ON p.show_id = s.show_id
        WHERE :newStart < (p.local_date_time + s.duration * INTERVAL '1 minute')
          AND :newEnd > p.local_date_time AND p.hall_id = :hallId
        """, nativeQuery = true)
    List<Performance> findOverlappingPerformances(
            @Param("newStart") LocalDateTime newStart,
            @Param("newEnd") LocalDateTime newEnd,
            @Param("hallId") Long hallId
            //@Param("performanceId") Long performanceId
    );
}


