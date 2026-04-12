package com.ecommerce.project.payload;

import com.ecommerce.project.model.PerformanceSeat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {

    private Long cartItemId;
    private CartDTO cart;
    private PerformanceDTO performanceDTO;
    private Integer quantity;
    private Double discount;
    private Double performancePrice;
    private PerformanceSeat performanceSeat;
}
