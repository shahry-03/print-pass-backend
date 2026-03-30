package com.printpass.user.entity;

import com.printpass.user.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "users") // Database mein is naam se table banega
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

//    @Column(nullable = false)
//    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password; // Baad mein hum isey encrypt karenge

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; // STUDENT ya VENDOR

//    // PrintPass Wallet ka balance
//    @Column(nullable = false)
//    private BigDecimal walletBalance;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
    }





//    @UpdateTimestamp
//    private LocalDateTime updatedAt;

    @Column(name = "is_active")
    private boolean isActive = true;
}

