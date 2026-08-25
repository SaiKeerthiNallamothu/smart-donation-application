package com.smartdonation.project.dto.response;

import com.smartdonation.project.enums.LicenseType;
import com.smartdonation.project.enums.LicenseVerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DrivingLicenseResponse {

    private Long id;
    private String licenseNumber;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String issuingState;
    private String documentUrl;
    private LicenseType licenseType;
    private LicenseVerificationStatus verificationStatus;
}
