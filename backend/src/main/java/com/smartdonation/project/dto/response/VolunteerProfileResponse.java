package com.smartdonation.project.dto.response;

import com.smartdonation.project.enums.AvailabilityStatus;
import com.smartdonation.project.enums.Gender;
import com.smartdonation.project.enums.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VolunteerProfileResponse {

    private Long id;

    // From User
    private String firstName;
    private String lastName;
    private String email;
    private String phone;

    // Volunteer-specific fields
    private LocalDate dob;
    private Gender gender;
    private String alternativePhone;
    private AvailabilityStatus availabilityStatus;
    private VehicleType vehicleType;

    private AddressResponse address;
    private DrivingLicenseResponse drivingLicense;

    private String message;
}
