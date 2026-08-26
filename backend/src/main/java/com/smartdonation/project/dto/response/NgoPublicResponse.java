package com.smartdonation.project.dto.response;

import com.smartdonation.project.enums.VerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Donor-safe response DTO for viewing verified NGOs.
 * Exposes only public information — no internal user data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NgoPublicResponse {

    private Long profileId;

    private String ngoName;

    private String description;

    private String website;

    private VerificationStatus verificationStatus;

    private AddressResponse address;
}
