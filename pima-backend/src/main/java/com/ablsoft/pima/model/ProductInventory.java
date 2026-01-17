package com.ablsoft.pima.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"productSku", "purchaseDate"}
        )
)
@Data
public class ProductInventory {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String productSku;

    private String productName;
    private String category;

    @Column(nullable = false)
    private LocalDate purchaseDate;

    private BigDecimal unitPrice;
    private Integer quantity;
}
