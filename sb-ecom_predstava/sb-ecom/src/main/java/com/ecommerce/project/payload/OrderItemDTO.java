package com.ecommerce.project.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {

    private String performanceName;

    private String rowLabel;

    private Integer seatNumber;

    private double discount;

    private double orderedPerformancePrice;
}
