package com.smartdonation.project.dto.request;

import com.smartdonation.project.enums.Gender;
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
public class DonorProfileRequest {

    @NotNull(message = "Date of birth is required")
    private LocalDate dob;

    @NotNull(message = "Gender is required")
    private Gender gender;

    @Pattern(regexp = "^(\\+91[\\-\\s]?)?[6-9]\\d{9}$",
            message = "Alternative phone must be a valid Indian mobile number")
    private String alternativePhone;

    @NotNull(message = "Address is required")
    @Valid
    private AddressRequest address;
}
