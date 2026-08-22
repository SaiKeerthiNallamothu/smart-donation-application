package com.smartdonation.project.dto.request;

import com.smartdonation.project.enums.VerificationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationStatusRequest {

    @NotNull(message = "Verification status is required")
    private VerificationStatus status;
}
