package com.smartdonation.project.controllers;

import com.smartdonation.project.common.exception.DuplicateResourceException;
import com.smartdonation.project.common.exception.ResourceNotFoundException;
import com.smartdonation.project.dto.request.DrivingLicenseRequest;
import com.smartdonation.project.dto.request.DrivingLicenseVerifyRequest;
import com.smartdonation.project.dto.request.VolunteerProfileRequest;
import com.smartdonation.project.dto.response.DrivingLicenseResponse;
import com.smartdonation.project.dto.response.VolunteerProfileResponse;
import com.smartdonation.project.dto.request.update.VolunteerProfileUpdateRequest;
import com.smartdonation.project.service.VolunteerProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class VolunteerProfileController {

    private final VolunteerProfileService volunteerProfileService;

    // ═══════════════════════════════════════════════════════════
    // VOLUNTEER → OWN PROFILE
    // ═══════════════════════════════════════════════════════════

    @PostMapping("/api/v1/volunteer/profile/create")
    public ResponseEntity<VolunteerProfileResponse> createProfile(
            Authentication authentication,
            @Valid @RequestBody VolunteerProfileRequest request)
            throws DuplicateResourceException {
        String email = authentication.getName();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(volunteerProfileService.createProfile(email, request));
    }

    @GetMapping("/api/v1/volunteer/profile/get")
    public ResponseEntity<VolunteerProfileResponse> getProfile(Authentication authentication)
            throws ResourceNotFoundException {
        String email = authentication.getName();
        return ResponseEntity.ok(volunteerProfileService.getProfile(email));
    }

    @PutMapping("/api/v1/volunteer/profile/update")
    public ResponseEntity<VolunteerProfileResponse> updateProfile(
            Authentication authentication,
            @Valid @RequestBody VolunteerProfileUpdateRequest request)
            throws ResourceNotFoundException {
        String email = authentication.getName();
        return ResponseEntity.ok(volunteerProfileService.updateProfile(email, request));
    }

    @DeleteMapping("/api/v1/volunteer/profile/delete")
    public ResponseEntity<Void> deleteProfile(Authentication authentication)
            throws ResourceNotFoundException {
        String email = authentication.getName();
        volunteerProfileService.deleteProfile(email);
        return ResponseEntity.noContent().build();
    }

    // ═══════════════════════════════════════════════════════════
    // VOLUNTEER → OWN DRIVING LICENCE
    // ═══════════════════════════════════════════════════════════

    @PostMapping("/api/v1/volunteer/license/submit")
    public ResponseEntity<DrivingLicenseResponse> submitLicense(
            Authentication authentication,
            @Valid @RequestBody DrivingLicenseRequest request)
            throws ResourceNotFoundException {
        String email = authentication.getName();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(volunteerProfileService.submitLicense(email, request));
    }

    @GetMapping("/api/v1/volunteer/license/get")
    public ResponseEntity<DrivingLicenseResponse> getOwnLicense(
            Authentication authentication)
            throws ResourceNotFoundException {
        String email = authentication.getName();
        return ResponseEntity.ok(volunteerProfileService.getOwnLicense(email));
    }

    // ═══════════════════════════════════════════════════════════
    // ADMIN → VOLUNTEER MANAGEMENT
    // ═══════════════════════════════════════════════════════════

    @GetMapping("/api/v1/admin/volunteers")
    public ResponseEntity<List<VolunteerProfileResponse>> getAllVolunteers() {
        return ResponseEntity.ok(volunteerProfileService.getAllVolunteerProfiles());
    }

    @GetMapping("/api/v1/admin/volunteers/pending")
    public ResponseEntity<List<VolunteerProfileResponse>> getPendingVolunteers() {
        return ResponseEntity.ok(volunteerProfileService.getPendingVolunteerProfiles());
    }

    @GetMapping("/api/v1/admin/volunteers/{id}")
    public ResponseEntity<VolunteerProfileResponse> getVolunteerById(@PathVariable Long id)
            throws ResourceNotFoundException {
        return ResponseEntity.ok(volunteerProfileService.getVolunteerProfileById(id));
    }

    @PatchMapping("/api/v1/admin/volunteers/{id}/license/verify")
    public ResponseEntity<DrivingLicenseResponse> verifyLicense(
            @PathVariable Long id,
            @Valid @RequestBody DrivingLicenseVerifyRequest request)
            throws ResourceNotFoundException {
        return ResponseEntity.ok(volunteerProfileService.verifyLicense(id, request));
    }

    @PatchMapping("/api/v1/admin/volunteers/{id}/license/reject")
    public ResponseEntity<DrivingLicenseResponse> rejectLicense(@PathVariable Long id)
            throws ResourceNotFoundException {
        return ResponseEntity.ok(volunteerProfileService.rejectLicense(id));
    }
}
