package com.printpass.shop.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "shops")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Yeh ID us user ki hogi jo VENDOR hai (user-service se aayegi)
    @Column(nullable = false)
    private Long vendorId;

    @Column(nullable = false)
    private String shopName;

    @Column(nullable = false)
    private String location; // Maslan: "MUET Canteen" ya "CS Dept"

    @Column(nullable = false)
    private BigDecimal perPageCost; // Ek page print karne ki qeemat

    @Column(nullable = false)
    private boolean isOpen; // Dukan abhi khuli hai ya band?

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}