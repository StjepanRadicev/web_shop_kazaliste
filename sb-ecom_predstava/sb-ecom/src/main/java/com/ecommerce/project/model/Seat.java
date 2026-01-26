package com.ecommerce.project.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name="seat",
        uniqueConstraints = @UniqueConstraint(
                name="uq_seat",
                columnNames={"hall_id","section","row_label","seat_number"}
        )
)
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seatId;

    @ManyToOne(fetch = FetchType.LAZY, optional=false)
    @JoinColumn(name="hall_id", nullable=false)
    private Hall hall;

    @Column(length=50)
    private String section;

    @Column(name="row_label", nullable=false, length=10)
    private String rowLabel;

    @Column(name="seat_number", nullable=false)
    private Integer seatNumber;
}
