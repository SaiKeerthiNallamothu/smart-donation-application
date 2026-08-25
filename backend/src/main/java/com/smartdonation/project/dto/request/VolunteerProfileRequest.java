package com.smartdonation.project.dto.request;

import com.smartdonation.project.enums.AvailabilityStatus;
import com.smartdonation.project.enums.Gender;
import com.smartdonation.project.enums.VehicleType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

import com.smartdonation.project.dto.request.DrivingLicenseRequest;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VolunteerProfileRequest {

    @NotNull(message = "Date of birth is required")
    private LocalDate dob;

    @NotNull(message = "Gender is required")
    private Gender gender;

    @Pattern(regexp = "^(\\+91[\\-\\s]?)?[6-9]\\d{9}$",
            message = "Alternative phone must be a valid Indian mobile number")
    private String alternativePhone;

    @NotNull(message = "Availability status is required")
    private AvailabilityStatus availabilityStatus;

    @NotNull(message = "Vehicle type is required")
    private VehicleType vehicleType;

    @NotNull(message = "Address is required")
    @Valid
    private AddressRequest address;

    @Valid
    @NotNull(message = "Driving license is required")
    private DrivingLicenseRequest drivingLicense;
}
