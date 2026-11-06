package com.ecommerce.project.payload;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
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

    @PositiveOrZero(message = "Vrijednost nije ispravna.")
    private Long duration;

    private String description;

    private String categoryName;
}
