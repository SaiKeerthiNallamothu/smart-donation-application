package com.smartdonation.project.dto.request;

import com.smartdonation.project.enums.LicenseType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DrivingLicenseVerifyRequest {

    @NotNull(message = "License type is required")
    private LicenseType licenseType;
}
