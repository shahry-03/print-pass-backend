package com.printpass.job.entity;

import com.printpass.job.enums.JobStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "print_jobs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrintJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long studentId; // Kisne order diya? (User Service se)

    @Column(nullable = true) // kiyunki hum version 1.0 me shop ka jhanjhat nahi chahte, toh isko optional rakhte hain. Future me agar zarurat padi toh isko mandatory bana denge.
    private Long shopId; // Kis dukan par order diya? (Shop Service se)

    @Column(nullable = false)
    private String documentName; // File ka naam (maslan "FYP_Report.pdf")

    @Column(nullable = false)
    private Integer totalPages; // Kitne pages hain?

    @Column(nullable = false)
    private boolean isColor; // Color print hai ya Black & White?

    @Column(nullable = false)
    private BigDecimal totalCost; // Total bill kitna bana?

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status; // Abhi order ka kya status hai?

    // Naya Column: 6-Digit Secret PIN
    @Column(nullable = false, unique = true, length = 6)
    private String pin;

    @CreationTimestamp
    private LocalDateTime createdAt;
}