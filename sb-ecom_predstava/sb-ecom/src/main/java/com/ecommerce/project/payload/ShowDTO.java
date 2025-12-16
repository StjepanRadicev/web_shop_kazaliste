package com.ecommerce.project.payload;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShowDTO {

    private Long showId;
    @NotNull
    private String showName;
    private Long duration;
    private String description;
    private String categoryName;
}
