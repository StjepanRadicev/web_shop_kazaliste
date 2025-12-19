package com.ecommerce.project.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Performance {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long performanceId;

    @Column(unique = true)
    @NotBlank
    private String performanceName;

    private String image;

    private String description;

    private Integer quantity;

    private double price;

    private double discount;

    private double specialPrice;

    private LocalDateTime localDateTime;

    @ManyToOne
    @JoinColumn(name = "show_id")
    private Show show;

    @ManyToOne
    @JoinColumn(name = "seller_id")
    private User user;

    @OneToMany(mappedBy = "performance",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            fetch = FetchType.EAGER)
    private List<CartItem> performances = new ArrayList<>();
}
