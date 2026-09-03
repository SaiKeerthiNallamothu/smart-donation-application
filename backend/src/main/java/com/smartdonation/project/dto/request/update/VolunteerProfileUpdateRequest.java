package com.smartdonation.project.dto.request.update;

import com.smartdonation.project.dto.request.AddressRequest;
import com.smartdonation.project.enums.AvailabilityStatus;
import com.smartdonation.project.enums.Gender;
import com.smartdonation.project.enums.VehicleType;
import jakarta.validation.constraints.Pattern;
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

    // All fields optional — partial update: only supplied fields are applied
    private String firstName;

    private String lastName;

    private String phone;

    private LocalDate dob;

    private Gender gender;

    @Pattern(regexp = "^(\\+91[\\-\\s]?)?[6-9]\\d{9}$",
            message = "Alternative phone must be a valid Indian mobile number")
    private String alternativePhone;

    private AvailabilityStatus availabilityStatus;

    private VehicleType vehicleType;

    // Address is optional; if provided, only non-null nested fields are updated
    private AddressRequest address;
}
