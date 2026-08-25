package com.smartdonation.project.dto.request.update;

import com.smartdonation.project.dto.request.AddressRequest;
import com.smartdonation.project.dto.request.DrivingLicenseRequest;
import com.smartdonation.project.enums.AvailabilityStatus;
import com.smartdonation.project.enums.Gender;
import com.smartdonation.project.enums.VehicleType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VolunteerProfileUpdateRequest {

    // Editable User fields
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @NotBlank(message = "Phone number is required")
    @Size(min = 10, max = 15, message = "Phone number must be between 10 and 15 characters")
    private String phone;

    // Volunteer-specific fields
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
