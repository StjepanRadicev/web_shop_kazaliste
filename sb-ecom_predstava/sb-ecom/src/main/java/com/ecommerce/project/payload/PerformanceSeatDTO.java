package com.ecommerce.project.payload;

import com.ecommerce.project.model.Performance;
import com.ecommerce.project.model.PerformanceSeatStatus;
import com.ecommerce.project.model.Seat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceSeatDTO {

    private PerformanceSeatStatus status;

    private double price;

    private String section;

    private String rowLabel;

    private Integer seatNumber;

    private String performanceName;
}

