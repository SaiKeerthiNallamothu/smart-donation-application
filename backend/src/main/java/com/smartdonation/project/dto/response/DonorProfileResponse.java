package com.smartdonation.project.dto.response;

import com.smartdonation.project.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonorProfileResponse {

    private Long profileId;

    private LocalDate dob;
    private Gender gender;
    private String alternativePhone;

    private UserResponseDto user;
    private AddressResponse address;

    private String message;
}
