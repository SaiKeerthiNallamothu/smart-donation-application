package com.smartdonation.project.controllers;

import com.smartdonation.project.common.exception.DuplicateResourceException;
import com.smartdonation.project.common.exception.ResourceNotFoundException;
import com.smartdonation.project.dto.request.DonorProfileRequest;
import com.smartdonation.project.dto.response.DonorProfileResponse;
import com.smartdonation.project.service.DonorProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/donor/profile")
@RequiredArgsConstructor
public class DonorProfileController {

    private final DonorProfileService donorProfileService;

    @PostMapping("/create")
    public ResponseEntity<DonorProfileResponse> createProfile(
            Authentication authentication,
            @Valid @RequestBody DonorProfileRequest request)
            throws DuplicateResourceException {
        String email = authentication.getName();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(donorProfileService.createProfile(email, request));
    }

    @GetMapping("/get")
    public ResponseEntity<DonorProfileResponse> getProfile(Authentication authentication)
            throws ResourceNotFoundException {
        String email = authentication.getName();
        return ResponseEntity.ok(donorProfileService.getProfile(email));
    }

    @PutMapping("/update")
    public ResponseEntity<DonorProfileResponse> updateProfile(
            Authentication authentication,
            @Valid @RequestBody DonorProfileRequest request)
            throws ResourceNotFoundException {
        String email = authentication.getName();
        return ResponseEntity.ok(donorProfileService.updateProfile(email, request));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteProfile(Authentication authentication)
            throws ResourceNotFoundException {
        String email = authentication.getName();
        donorProfileService.deleteProfile(email);
        return ResponseEntity.noContent().build();
    }
}
