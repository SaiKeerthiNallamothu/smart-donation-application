package com.smartdonation.project.dto.response;

import com.smartdonation.project.enums.VerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NgoProfileResponse {

    private Long profileId;

    private String ngoName;

    private String registrationNumber;

    private String contactPersonName;

    private String description;

    private String website;

    private VerificationStatus verificationStatus;

    private UserResponseDto user;

    private AddressResponse address;

    private String message;
}
