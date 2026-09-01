package com.smartdonation.project.controllers;

import com.smartdonation.project.common.exception.DuplicateResourceException;
import com.smartdonation.project.common.exception.ResourceNotFoundException;
import com.smartdonation.project.dto.request.DonorProfileRequest;
import com.smartdonation.project.dto.request.update.DonorProfileUpdateRequest;
import com.smartdonation.project.dto.response.DonorProfileResponse;
import com.smartdonation.project.service.DonorProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DonorProfileController {

    private final DonorProfileService donorProfileService;

    @PostMapping("/api/v1/donor/profile/create")
    public ResponseEntity<DonorProfileResponse> createProfile(
            Authentication authentication,
            @Valid @RequestBody DonorProfileRequest request)
            throws DuplicateResourceException {
        String email = authentication.getName();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(donorProfileService.createProfile(email, request));
    }

    @GetMapping("/api/v1/donor/profile/get")
    public ResponseEntity<DonorProfileResponse> getProfile(Authentication authentication)
            throws ResourceNotFoundException {
        String email = authentication.getName();
        return ResponseEntity.ok(donorProfileService.getProfile(email));
    }

    @PutMapping("/api/v1/donor/profile/update")
    public ResponseEntity<DonorProfileResponse> updateProfile(
            Authentication authentication,
            @Valid @RequestBody DonorProfileUpdateRequest request)
            throws ResourceNotFoundException {
        String email = authentication.getName();
        return ResponseEntity.ok(donorProfileService.updateProfile(email, request));
    }

    @DeleteMapping("/api/v1/donor/profile/delete")
    public ResponseEntity<Void> deleteProfile(Authentication authentication)
            throws ResourceNotFoundException {
        String email = authentication.getName();
        donorProfileService.deleteProfile(email);
        return ResponseEntity.noContent().build();
    }

    // ═══════════════════════════════════════════════════════════
    // ADMIN → DONOR MANAGEMENT
    // ═══════════════════════════════════════════════════════════

    @GetMapping("/api/v1/admin/donors")
    public ResponseEntity<List<DonorProfileResponse>> getAllDonors() {
        return ResponseEntity.ok(donorProfileService.getAllDonorProfiles());
    }

    @GetMapping("/api/v1/admin/donors/{id}")
    public ResponseEntity<DonorProfileResponse> getDonorById(@PathVariable Long id)
            throws ResourceNotFoundException {
        return ResponseEntity.ok(donorProfileService.getDonorProfileById(id));
    }
}
