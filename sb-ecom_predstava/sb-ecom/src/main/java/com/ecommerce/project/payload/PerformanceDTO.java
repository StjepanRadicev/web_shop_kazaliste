package com.ecommerce.project.payload;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceDTO {

    private Long performanceId;

    @NotBlank(message = "Naziv proizvoda je obavezan.")
    @Size(max = 100, message = "Naziv ne smije imati više od 100 znakova.")
    private String performanceName;

    @URL(message = "Slika mora biti ispravna URL adresa.")
    @Size(max = 255, message = "URL slike ne smije biti duži od 255 znakova.")
    private String image;

    @NotNull(message = "Količina je obavezna.")
    @Min(value = 0, message = "Količina ne može biti manja od 0.")
    private Integer quantity;

    private Integer quantityInCart;

    private Integer totalInCarts;

    @PositiveOrZero(message = "Cijena mora biti 0 ili više.")
    private double price;

    @Size(max = 500, message = "Opis može imati najviše 500 znakova.")
    private String description;

    @PositiveOrZero(message = "Popust mora biti 0 ili više.")
    @Max(value = 100, message = "Popust ne može biti veći od 100%.")
    private double discount;

    @PositiveOrZero(message = "Posebna cijena mora biti 0 ili više.")
    private double specialPrice;

    @FutureOrPresent(message = "Datum mora biti sadašnji ili budući.")
    private LocalDateTime localDateTime;

    private Long showId;

    private String categoryName;

    private String showName;























//    private Long productId;
//    @NotBlank
//    private String productName;
//    private String image;
//    private Integer quantity;
//    @PositiveOrZero
//    private double price;
//    @Size(max = 500)
//    private String description;
//    private double discount;
//    private double specialPrice;
//    private Long showId;
//    private String categoryName;
//    private String showName;
//    private LocalDateTime localDateTime;



}






















