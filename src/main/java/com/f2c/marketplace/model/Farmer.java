package com.f2c.marketplace.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "farmers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Farmer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @Column(name = "farm_name", nullable = false)
    private String farmName;

    @Column(name = "farm_address", nullable = false)
    private String farmAddress;

    @Column(name = "verification_status", nullable = false)
    private String verificationStatus = "PENDING"; // PENDING, APPROVED, REJECTED

    @Column(name = "license_number")
    private String licenseNumber;

    @Column(nullable = false)
    private Double revenue = 0.0;
}
