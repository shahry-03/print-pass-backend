package com.printpass.user.entity;
import jakarta.persistence.*;
import lombok.*;


import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "users") // Database mein is naam se table banega
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password; // Baad mein hum isey encrypt karenge

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; // STUDENT ya VENDOR

    // PrintPass Wallet ka balance
    @Column(nullable = false)
    private BigDecimal walletBalance;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

