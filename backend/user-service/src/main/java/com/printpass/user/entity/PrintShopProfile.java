package com.printpass.user.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "print_shop_profile",
        indexes = {
                @Index(name = "idx_user_id", columnList = "user_id"),
                @Index(name = "idx_shop_name", columnList = "shop_name"),
                @Index(name = "idx_city", columnList = "city"),
                @Index(name = "idx_is_verified", columnList = "is_verified")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrintShopProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude  // Recursion se bachne ke liye (Best Practice)
    private User user;

    @NotBlank
    @Size(max = 100)
    @Column(name = "shop_name", nullable = false)
    private String shopName;

    @NotBlank
    @Size(max = 100)
    @Column(name = "owner_name", nullable = false)
    private String ownerName;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number format")
    @Column(nullable = false)
    private String phone;


    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String address;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false)
    private String city;

    @Column(name = "is_verified", nullable = false)
    private boolean isVerified = false; // ← Admin verify karega

    // PrintShopProfile Entity mein add karo
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true; // Soft Delete ke liye (Best Practice)

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
    }

}
