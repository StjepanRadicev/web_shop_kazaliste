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

    private String section;

    private double discount;

    private double orderedPerformancePrice;
}
