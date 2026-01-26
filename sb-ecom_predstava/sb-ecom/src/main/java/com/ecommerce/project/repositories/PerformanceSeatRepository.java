package com.ecommerce.project.repositories;

import com.ecommerce.project.model.PerformanceSeat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
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

}