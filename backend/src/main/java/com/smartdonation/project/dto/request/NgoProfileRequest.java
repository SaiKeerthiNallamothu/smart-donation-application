package com.smartdonation.project.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NgoProfileRequest {

    @NotBlank(message = "NGO name is required")
    @Size(min = 2, max = 150, message = "NGO name must be between 2 and 150 characters")
    private String ngoName;

    @NotBlank(message = "Registration number is required")
    @Size(max = 50, message = "Registration number must not exceed 50 characters")
    private String registrationNumber;

    @NotBlank(message = "Contact person name is required")
    @Size(min = 2, max = 100, message = "Contact person name must be between 2 and 100 characters")
    private String contactPersonName;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    private String website;

    @NotNull(message = "Address is required")
    @Valid
    private AddressRequest address;
}
