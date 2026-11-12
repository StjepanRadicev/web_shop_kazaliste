package com.ecommerce.project.payload;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlySoldTicketsDTO {

    private Integer year;
    private Integer month;
    private Long soldTickets;
}

