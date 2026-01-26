package com.ecommerce.project.payload;

import com.ecommerce.project.model.PerformanceSeatStatus;
import lombok.Data;

@Data
public class SeatDTO {

    private Long performanceSeatId;
    private Long seatId;
    private String section;
    private String rowLabel;
    private Integer seatNumber;
    private PerformanceSeatStatus status;
    private Double price;
}
