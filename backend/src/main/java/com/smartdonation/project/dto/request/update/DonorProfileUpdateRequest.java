package com.smartdonation.project.dto.request.update;

import com.smartdonation.project.dto.request.AddressRequest;
import com.smartdonation.project.enums.Gender;
import jakarta.validation.Valid;
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
public class DonorProfileUpdateRequest {

    // Editable User fields (all nullable for partial update)
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @Pattern(regexp = "^(\\+91[\\-\\s]?)?[6-9]\\d{9}$",
            message = "Phone number must be a valid Indian mobile number (e.g. +91 98765 43210 or 9876543210)")
    private String phone;

    // Donor-specific fields (all nullable for partial update)
    private LocalDate dob;

    private Gender gender;

    @Pattern(regexp = "^(\\+91[\\-\\s]?)?[6-9]\\d{9}$",
            message = "Alternative phone must be a valid Indian mobile number")
    private String alternativePhone;

    @Valid
    private AddressRequest address;
}
