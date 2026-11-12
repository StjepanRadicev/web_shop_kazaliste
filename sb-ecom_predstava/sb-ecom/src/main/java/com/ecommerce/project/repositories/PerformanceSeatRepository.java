package com.ecommerce.project.repositories;

import com.ecommerce.project.model.PerformanceSeat;
import com.ecommerce.project.model.PerformanceSeatStatus;
import com.ecommerce.project.model.PerformanceStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PerformanceSeatRepository extends JpaRepository<PerformanceSeat, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    select ps from PerformanceSeat ps
    where ps.performance.id = :performanceId and ps.seat.id = :seatId
    """)
    Optional<PerformanceSeat> findForUpdate(Long performanceId, Long seatId);

    @Query("""
    select ps from PerformanceSeat ps
    where ps.performance.id = :performanceId
    """)
    List<PerformanceSeat> findByPerformanceId(Long performanceId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select ps from PerformanceSeat ps where ps.performanceSeatId = :psId")
    Optional<PerformanceSeat> findByIdForUpdate(@Param("psId") Long performanceSeatId);


    @Query("""
    SELECT COUNT(ps) FROM PerformanceSeat ps
    WHERE ps.performance.id = :performanceId
    AND ps.status = :status
    """)
    int countSeatsByPerformanceIdAndStatus(
            @Param("performanceId") Long performanceId,
            @Param("status") PerformanceSeatStatus status
    );

    boolean existsByPerformance_PerformanceIdAndStatus(
            Long performanceId,
            PerformanceSeatStatus status
    );

    List<PerformanceSeat> findByPerformance_PerformanceIdAndStatus(
            Long performanceId,
            PerformanceSeatStatus status
    );


    @Query("""
    SELECT
        ps.performance.show.showId,
        ps.performance.show.showName,
        COUNT(DISTINCT ps.performance.performanceId),
        COUNT(ps),
        SUM(CASE WHEN ps.status = :soldStatus THEN 1 ELSE 0 END)
    FROM PerformanceSeat ps
    WHERE ps.performance.status IN :statuses
    GROUP BY
        ps.performance.show.showId,
        ps.performance.show.showName
    ORDER BY
        SUM(CASE WHEN ps.status = :soldStatus THEN 1 ELSE 0 END) DESC
    """)
    List<Object[]> getShowOccupancy(
            @Param("soldStatus") PerformanceSeatStatus soldStatus,
            @Param("statuses") List<PerformanceStatus> statuses
    );


    @Modifying
    @Query("""
    DELETE FROM PerformanceSeat ps
    WHERE ps.performance.performanceId = :performanceId
""")
    int deleteByPerformanceId(@Param("performanceId") Long performanceId);

}