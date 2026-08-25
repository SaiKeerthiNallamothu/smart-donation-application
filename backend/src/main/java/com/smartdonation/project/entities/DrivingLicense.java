package com.smartdonation.project.entities;

import com.smartdonation.project.enums.LicenseType;
import com.smartdonation.project.enums.LicenseVerificationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "driving_licenses")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DrivingLicense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String licenseNumber;

    @Column(nullable = false)
    private LocalDate issueDate;

    @Column(nullable = false)
    private LocalDate expiryDate;

    @Column(nullable = false, length = 100)
    private String issuingState;

    @Column(length = 500)
    private String documentUrl;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private LicenseType licenseType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private LicenseVerificationStatus verificationStatus = LicenseVerificationStatus.PENDING;
}
