package com.f2c.marketplace.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_quality_scans")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductQualityScan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = true)
    private Product product;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "product_type", nullable = false)
    private String productType;

    @Column(name = "quality_score", nullable = false)
    private Integer qualityScore;

    @Column(name = "quality_grade", nullable = false)
    private String qualityGrade;

    @Column(nullable = false)
    private String freshness;

    @Column(name = "visible_defects", columnDefinition = "TEXT")
    private String visibleDefects;

    @Column(columnDefinition = "TEXT")
    private String recommendation;

    @Column(name = "scanned_at", nullable = false, updatable = false)
    private LocalDateTime scannedAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        scannedAt = LocalDateTime.now();
    }
}
